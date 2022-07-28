/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.detection.anomaly.utils;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyUtils {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyUtils.class);

  /**
   * Safely and quietly shutdown executor service. This method waits until all threads are complete,
   * or timeout occurs (5-minutes), or the current thread is interrupted, whichever happens first.
   *
   * @param executorService the executor service to be shutdown.
   * @param ownerClass the class that owns the executor service; it could be null.
   */
  public static void safelyShutdownExecutionService(ExecutorService executorService,
      Class ownerClass) {
    safelyShutdownExecutionService(executorService, 300, ownerClass);
  }

  /**
   * Safely and quietly shutdown executor service. This method waits until all threads are complete,
   * or number of retries is reached, or the current thread is interrupted, whichever happens first.
   *
   * @param executorService the executor service to be shutdown.
   * @param maxWaitTimeInSeconds max wait time for threads that are still running.
   * @param ownerClass the class that owns the executor service; it could be null.
   */
  public static void safelyShutdownExecutionService(ExecutorService executorService,
      int maxWaitTimeInSeconds,
      Class ownerClass) {
    if (executorService == null) {
      return;
    }
    executorService.shutdown(); // Prevent new tasks from being submitted
    try {
      // If not all threads are complete, then a retry loop waits until all threads are complete, or timeout occurs,
      // or the current thread is interrupted, whichever happens first.
      for (int retryCount = 0; retryCount < maxWaitTimeInSeconds; ++retryCount) {
        // Wait a while for existing tasks to terminate
        if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
          // Force terminate all currently executing tasks if they support such operation
          executorService.shutdownNow();
          if (retryCount % 10 == 0) {
            if (ownerClass != null) {
              LOG.info("Trying to terminate thread pool for class {}", ownerClass.getSimpleName());
            } else {
              LOG.info("Trying to terminate thread pool: {}.", executorService);
            }
          }
        } else {
          break; // break out retry loop if all threads are complete
        }
      }
    } catch (InterruptedException e) { // If current thread is interrupted
      executorService.shutdownNow(); // Interrupt all currently executing tasks for the last time
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Returns the filter set to query the time series on UI. The filter set is constructed by
   * combining the dimension
   * information of the given anomaly and the filter set from its corresponding anomaly function.
   *
   * For instance, assume that the dimension from the detected anomaly is {"country":"US"} and the
   * filter on its
   * anomaly function is {"country":["US", "IN"],"page_key":["p1,p2"]}, then the returned filter set
   * for querying
   * is {"country":["US"],"page_key":["p1,p2"]}.
   *
   * @param mergedAnomaly the target anomaly for which we want to generate the query filter set.
   * @return the filter set for querying the time series that produce the anomaly.
   */
  public static Multimap<String, String> generateFilterSetForTimeSeriesQuery(
      MergedAnomalyResultDTO mergedAnomaly) {
    AnomalyFunctionDTO anomalyFunctionDTO = mergedAnomaly.getAnomalyFunction();
    Multimap<String, String> filterSet = anomalyFunctionDTO.getFilterSet();
    Multimap<String, String> newFilterSet = generateFilterSetWithDimensionMap(
        mergedAnomaly.getDimensions(), filterSet);
    return newFilterSet;
  }

  public static Multimap<String, String> generateFilterSetWithDimensionMap(
      DimensionMap dimensionMap,
      Multimap<String, String> filterSet) {

    Multimap<String, String> newFilterSet = HashMultimap.create();

    // Dimension map gives more specified dimension information than filter set (i.e., Dimension Map should be a subset
    // of filterSet), so it needs to be processed first.
    if (MapUtils.isNotEmpty(dimensionMap)) {
      for (Map.Entry<String, String> dimensionMapEntry : dimensionMap.entrySet()) {
        newFilterSet.put(dimensionMapEntry.getKey(), dimensionMapEntry.getValue());
      }
    }

    if (filterSet != null && filterSet.size() != 0) {
      for (String key : filterSet.keySet()) {
        if (!newFilterSet.containsKey(key)) {
          newFilterSet.putAll(key, filterSet.get(key));
        }
      }
    }

    return newFilterSet;
  }
}
