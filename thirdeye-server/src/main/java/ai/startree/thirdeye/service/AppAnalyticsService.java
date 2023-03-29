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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;
import static ai.startree.thirdeye.spi.detection.AnomalyFeedbackType.ANOMALY;
import static ai.startree.thirdeye.spi.detection.AnomalyFeedbackType.ANOMALY_EXPECTED;
import static ai.startree.thirdeye.spi.detection.AnomalyFeedbackType.ANOMALY_NEW_TREND;
import static ai.startree.thirdeye.spi.detection.AnomalyFeedbackType.NOT_ANOMALY;
import static ai.startree.thirdeye.spi.detection.AnomalyFeedbackType.NO_FEEDBACK;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.core.ConfusionMatrix;
import ai.startree.thirdeye.core.MonitoredMetricWrapper;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.api.AppAnalyticsApi;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppAnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AppAnalyticsService.class);

  private final AlertManager alertManager;
  private final AlertTemplateRenderer renderer;
  private final AnomalyManager anomalyManager;

  public Supplier<List<AnomalyFeedback>> anomalyFeedbacksSupplier =
      Suppliers.memoizeWithExpiration(this::getAllAnomalyFeedbacks,
          METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES)::get;
  public Supplier<Set<MonitoredMetricWrapper>> uniqueMonitoredMetricsSupplier =
      Suppliers.memoizeWithExpiration(this::getUniqueMonitoredMetrics,
          METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES)::get;

  @Inject
  public AppAnalyticsService(final AlertManager alertManager,
      final AlertTemplateRenderer renderer,
      final AnomalyManager anomalyManager,
      final MetricRegistry metricRegistry) {
    this.alertManager = alertManager;
    this.anomalyManager = anomalyManager;
    this.renderer = renderer;
    registerMetrics(metricRegistry);
  }

  public static String appVersion() {
    return AppAnalyticsService.class.getPackage().getImplementationVersion();
  }

  private static Predicate notIgnored() {
    return Predicate.EQ("ignored", false);
  }

  private void registerMetrics(final MetricRegistry metricRegistry) {
    metricRegistry.register("nMonitoredMetrics",
        new CachedGauge<Integer>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Integer loadValue() {
            return uniqueMonitoredMetricsCount();
          }
        });
    metricRegistry.register("anomalyCountTotal",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return countTotal(null);
          }
        });
    metricRegistry.register("anomalyFeedbackCount",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return countFeedbacks(null);
          }
        });
    metricRegistry.register("anomalyPrecision",
        new CachedGauge<Double>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Double loadValue() {
            return computeConfusionMatrixForAnomalies().getPrecision();
          }
        });
    metricRegistry.register("anomalyResponseRate",
        new CachedGauge<Double>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Double loadValue() {
            return computeConfusionMatrixForAnomalies().getResponseRate();
          }
        });
  }

  public Integer uniqueMonitoredMetricsCount() {
    return uniqueMonitoredMetricsSupplier.get().size();
  }

  private Set<MonitoredMetricWrapper> getUniqueMonitoredMetrics() {
    return alertManager.findAllActive().stream()
        .map(this::getMetadata)
        .filter(Objects::nonNull)
        .map(this::wrapMonitoredMetric)
        .collect(Collectors.toSet());
  }

  private AlertMetadataDTO getMetadata(final AlertDTO alertDTO) {
    try {
      // Interval does not have significance in this case, just a placeholder.
      return renderer.renderAlert(alertDTO, new Interval(1L, 2L)).getMetadata();
    } catch (final IOException | ClassNotFoundException | BadRequestException e) {
      log.warn(String.format("Trouble while rendering alert, %s. id : %d",
          alertDTO.getName(),
          alertDTO.getId()), e);
      return null;
    }
  }

  private MonitoredMetricWrapper wrapMonitoredMetric(final AlertMetadataDTO metadata) {
    return new MonitoredMetricWrapper()
        .setMetric(metadata.getMetric().getName())
        .setDataset(metadata.getDataset().getDataset())
        .setDatasource(metadata.getDatasource().getName());
  }

  public ConfusionMatrix computeConfusionMatrixForAnomalies() {
    final ConfusionMatrix matrix = new ConfusionMatrix();
    // filter to get anomalies without feedback and which are not ignored
    final Predicate unclassified = Predicate.AND(notIgnored(),
        Predicate.EQ("anomalyFeedbackId", 0));
    matrix.addUnclassified((int) anomalyManager.countParentAnomalies(
        new DaoFilter().setPredicate(unclassified)));

    final List<AnomalyFeedback> allFeedbacks = anomalyFeedbacksSupplier.get();
    final Map<AnomalyFeedbackType, Long> typeMap = aggregateFeedbackTypes(allFeedbacks);
    matrix.addUnclassified(Math.toIntExact(typeMap.get(NO_FEEDBACK)));
    matrix.addFalsePositive(Math.toIntExact(typeMap.get(NOT_ANOMALY)));
    matrix.addTruePositive(Math.toIntExact(typeMap.get(ANOMALY))
        + Math.toIntExact(typeMap.get(ANOMALY_EXPECTED))
        + Math.toIntExact(typeMap.get(ANOMALY_NEW_TREND)));
    return matrix;
  }

  private List<AnomalyFeedback> getAllAnomalyFeedbacks() {
    return getAnomalyFeedbacks(null);
  }

  private List<AnomalyFeedback> getAnomalyFeedbacks(final Predicate predicate) {
    Predicate finalPredicate = notIgnored();
    if (predicate != null) {
      finalPredicate = Predicate.AND(finalPredicate, predicate);
    }
    return anomalyManager.findParentAnomaliesWithFeedback(
            new DaoFilter().setPredicate(finalPredicate)).stream()
        .map(AnomalyDTO::getFeedback)
        .collect(Collectors.toList());
  }

  public AnomalyStatsApi computeAnomalyStats(final Predicate predicate) {
    final List<AnomalyFeedback> allFeedbacks = predicate == null
        ? anomalyFeedbacksSupplier.get()
        : getAnomalyFeedbacks(predicate);
    return new AnomalyStatsApi()
        .setTotalCount(countTotal(predicate))
        .setCountWithFeedback(countFeedbacks(predicate))
        .setFeedbackStats(aggregateFeedbackTypes(allFeedbacks));
  }

  private Long countTotal(final Predicate predicate) {
    Predicate finalPredicate = Predicate.AND(notIgnored(),
        Predicate.EQ("child", false));
    if (predicate != null) {
      finalPredicate = Predicate.AND(finalPredicate, predicate);
    }
    return anomalyManager.countParentAnomalies(new DaoFilter().setPredicate(finalPredicate));
  }

  private Long countFeedbacks(final Predicate predicate) {
    Predicate finalPredicate = Predicate.NEQ("anomalyFeedbackId", 0);
    if (predicate != null) {
      finalPredicate = Predicate.AND(finalPredicate, predicate);
    }
    return countTotal(finalPredicate);
  }

  private Map<AnomalyFeedbackType, Long> aggregateFeedbackTypes(
      final List<AnomalyFeedback> feedbacks) {
    final Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();
    for (final AnomalyFeedbackType type : AnomalyFeedbackType.values()) {
      feedbackStats.put(type, 0L);
    }
    feedbacks.forEach(feedback -> {
      final AnomalyFeedbackType type = feedback.getFeedbackType();
      final long count = feedbackStats.get(type);
      feedbackStats.put(type, count + 1);
    });
    return feedbackStats;
  }

  public AppAnalyticsApi getAppAnalytics(final Long startTime, final Long endTime) {
    final List<Predicate> predicates = new ArrayList<>();
    optional(startTime).ifPresent(start -> predicates.add(Predicate.GE("startTime", startTime)));
    optional(endTime).ifPresent(end -> predicates.add(Predicate.LE("endTime", endTime)));
    final Predicate predicate = predicates.isEmpty()
        ? null : Predicate.AND(predicates.toArray(Predicate[]::new));
    return new AppAnalyticsApi()
        .setVersion(appVersion())
        .setnMonitoredMetrics(uniqueMonitoredMetricsCount())
        .setAnomalyStats(computeAnomalyStats(predicate));
  }
}
