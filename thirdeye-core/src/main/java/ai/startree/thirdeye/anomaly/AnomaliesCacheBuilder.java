/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.anomaly;

import ai.startree.thirdeye.config.CacheConfig;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.model.AnomalySlice;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essentially a fetcher for fetching anomalies from cache/datasource.
 * The cache holds anomalies information per Anomaly Slices
 */
@Singleton
public class AnomaliesCacheBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(AnomaliesCacheBuilder.class);

  private static final String PROP_DETECTION_CONFIG_ID = "detectionConfigId";
  // Timeout to fetch anomalies from data source
  private static final long TIMEOUT = 60000;

  private final LoadingCache<AnomalySlice, Collection<MergedAnomalyResultDTO>> cache;
  private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
      "anomalies-cache-%d").build());
  private final MergedAnomalyResultManager anomalyDAO;
  private final CacheConfig cacheConfig;

  @Inject
  public AnomaliesCacheBuilder(final MergedAnomalyResultManager anomalyDAO,
      final CacheConfig cacheConfig) {
    this.anomalyDAO = anomalyDAO;
    this.cache = initCache();
    this.cacheConfig = cacheConfig;
  }

  private LoadingCache<AnomalySlice, Collection<MergedAnomalyResultDTO>> initCache() {
    LOG.info("Initializing anomalies cache");
    return CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(10000)
        .build(new CacheLoader<AnomalySlice, Collection<MergedAnomalyResultDTO>>() {
          @Override
          public Collection<MergedAnomalyResultDTO> load(AnomalySlice slice) {
            return loadAnomalies(Collections.singleton(slice)).get(slice);
          }

          @Override
          public Map<AnomalySlice, Collection<MergedAnomalyResultDTO>> loadAll(
              Iterable<? extends AnomalySlice> slices) {
            return loadAnomalies(Lists.newArrayList(slices));
          }
        });
  }

  public Collection<MergedAnomalyResultDTO> fetchSlice(AnomalySlice slice)
      throws ExecutionException {
    if (cacheConfig.useInMemoryCache()) {
      return this.cache.get(slice);
    } else {
      return loadAnomalies(Collections.singleton(slice)).get(slice);
    }
  }

  private Map<AnomalySlice, Collection<MergedAnomalyResultDTO>> loadAnomalies(
      Collection<AnomalySlice> slices) {
    Map<AnomalySlice, Collection<MergedAnomalyResultDTO>> output = new HashMap<>();
    try {
      long ts = System.currentTimeMillis();

      // if the anomalies are already in cache, return directly
      if (cacheConfig.useInMemoryCache()) {
        for (AnomalySlice slice : slices) {
          for (Map.Entry<AnomalySlice, Collection<MergedAnomalyResultDTO>> entry : this.cache
              .asMap().entrySet()) {
            // if the anomaly slice is already in cache, return directly. Otherwise fetch from data source.
            if (entry.getKey().containSlice(slice)) {
              output.computeIfAbsent(slice, k -> new ArrayList<>());
              for (MergedAnomalyResultDTO anomaly : entry.getValue()) {
                if (slice.match(anomaly)) {
                  output.get(slice).add(anomaly);
                }
              }
              break;
            }
          }
        }
      }

      // if not in cache, fetch from data source
      Map<AnomalySlice, Future<Collection<MergedAnomalyResultDTO>>> futures = new HashMap<>();
      for (AnomalySlice slice : slices) {
        if (!output.containsKey(slice)) {
          futures.put(slice, this.executor.submit(() -> {
            List<Predicate> predicates = DetectionUtils
                .buildPredicatesOnTime(slice.getStart(), slice.getEnd());

            if (slice.getDetectionId() >= 0) {
              predicates.add(Predicate.EQ(PROP_DETECTION_CONFIG_ID, slice.getDetectionId()));
            }

            if (predicates.isEmpty()) {
              throw new IllegalArgumentException(
                  "Must provide at least one of start, end, or " + PROP_DETECTION_CONFIG_ID);
            }

            Collection<MergedAnomalyResultDTO> anomalies = anomalyDAO
                .findByPredicate(DetectionUtils.AND(predicates));
            anomalies.removeIf(anomaly -> !slice.match(anomaly));

            return anomalies;
          }));
        }
      }

      for (AnomalySlice slice : slices) {
        if (futures.get(slice) != null) {
          output.put(slice, futures.get(slice).get(TIMEOUT, TimeUnit.MILLISECONDS));
        }
      }

      int anomalies = output.values().stream().mapToInt(Collection::size).sum();
      LOG.info(
          "Fetched {} anomalies, from {} slices, took {} milliseconds, {} slices hit cache, {} slices missed cache",
          anomalies,
          slices.size(),
          System.currentTimeMillis() - ts,
          (slices.size() - futures.size()),
          futures.size());
    } catch (TimeoutException e) {
      LOG.error("Timeout when fetching anomalies so assuming the result is empty.", e);
      for (AnomalySlice slice : slices) {
        output.put(slice, Collections.emptyList());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return output;
  }

  public void cleanCache() {
    if (this.cache != null) {
      this.cache.cleanUp();
    }
  }
}
