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
package ai.startree.thirdeye.util;

import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThirdEyeUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeUtils.class);
  private static final String PROP_METRIC_URNS_KEY = "metricUrn";
  private static final String PROP_NESTED_METRIC_URNS_KEY = "nestedMetricUrns";
  private static final String PROP_NESTED_PROPERTIES_KEY = "nested";

  @Deprecated
  public static List<DatasetConfigDTO> getDatasetConfigsFromMetricUrn(String metricUrn,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    MetricEntity me = MetricEntity.fromURN(metricUrn);
    MetricConfigDTO metricConfig = metricConfigManager.findById(me.getId());
    if (metricConfig == null) {
      return new ArrayList<>();
    }
    return Collections.singletonList(datasetConfigManager.findByDataset(metricConfig.getDataset()));
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be up to 4 decimals
   * For values >= 0.1, use 2 decimals (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values < 0.1, use 3 decimals (eg. 0.08, 0.071, 0.0123)
   *
   * @param value any double value
   * @return the rounded double value
   */
  public static Double getRoundedDouble(Double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return Double.NaN;
    }
    if (value >= 0.1) {
      return Math.round(value * (Math.pow(10, 2))) / (Math.pow(10, 2));
    } else {
      return Math.round(value * (Math.pow(10, 3))) / (Math.pow(10, 3));
    }
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be upto 4 decimals
   * For values gte 0.1, use ##.## (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values lt 0.1 and gte 0.01, use ##.### (eg. 0.08, 0.071, 0.0123)
   * For values lt 0.01 and gte 0.001, use ##.#### (eg. 0.001, 0.00367)
   * This function ensures we don't prematurely round off double values to a fixed format, and make
   * it 0.00 or lose out information
   */
  public static String getRoundedValue(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      if (Double.isNaN(value)) {
        return Double.toString(Double.NaN);
      }
      if (value > 0) {
        return Double.toString(Double.POSITIVE_INFINITY);
      } else {
        return Double.toString(Double.NEGATIVE_INFINITY);
      }
    }
    StringBuffer decimalFormatBuffer = new StringBuffer(Constants.TWO_DECIMALS_FORMAT);
    double compareValue = 0.1;
    while (value > 0 && value < compareValue && !decimalFormatBuffer.toString().equals(
        Constants.MAX_DECIMALS_FORMAT)) {
      decimalFormatBuffer.append(Constants.DECIMALS_FORMAT_TOKEN);
      compareValue = compareValue * 0.1;
    }
    DecimalFormat decimalFormat = new DecimalFormat(decimalFormatBuffer.toString());

    return decimalFormat.format(value);
  }

  /**
   * Parse job name to get the detection id
   */
  public static long getDetectionIdFromJobName(String jobName) {
    String[] parts = jobName.split("_");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid job name: " + jobName);
    }
    return Long.parseLong(parts[1]);
  }

  /**
   * Extract the list of metric urns in the detection config properties
   *
   * @param properties the detection config properties
   * @return the list of metric urns
   */
  public static Set<String> extractMetricUrnsFromProperties(Map<String, Object> properties) {
    Set<String> metricUrns = new HashSet<>();
    if (properties == null) {
      return metricUrns;
    }
    if (properties.containsKey(PROP_METRIC_URNS_KEY)) {
      metricUrns.add((String) properties.get(PROP_METRIC_URNS_KEY));
    }
    if (properties.containsKey(PROP_NESTED_METRIC_URNS_KEY)) {
      metricUrns.addAll(ConfigUtils.getList(properties.get(PROP_NESTED_METRIC_URNS_KEY)));
    }
    List<Map<String, Object>> nestedProperties = ConfigUtils
        .getList(properties.get(PROP_NESTED_PROPERTIES_KEY));
    // extract the metric urns recursively from the nested properties
    for (Map<String, Object> nestedProperty : nestedProperties) {
      metricUrns.addAll(extractMetricUrnsFromProperties(nestedProperty));
    }
    return metricUrns;
  }

  /**
   * Safely and quietly shutdown executor service. This method waits until all threads are
   * complete,
   * or timeout occurs or the current thread is interrupted, whichever happens first.
   *
   * <a
   * href="https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice">...</a>
   *
   * @param executorService the executor service to be shutdown.
   */
  public static void shutdownExecutionService(ExecutorService executorService) {
    final Duration timeout = Duration.ofSeconds(15);
    if (executorService == null) {
      return;
    }

    // Prevent new tasks from being submitted
    executorService.shutdown();
    try {
      // wait for tasks to drain
      if (!executorService.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS)) {

        // failed to terminate. shutdown all active tasks now.
        final List<Runnable> runnables = executorService.shutdownNow();
        if (!runnables.isEmpty()) {
          LOG.error(String.format("%d tasks pending. Trying one last time.",
              runnables.size()));
        }

        // Wait a while for tasks to respond to being cancelled
        if (!executorService.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS)) {
          LOG.error("Executor Service did not terminate. " + executorService);
        }
      }

    } catch (InterruptedException e) { // If current thread is interrupted
      // Interrupt all currently executing tasks for the last time
      final List<Runnable> runnables = executorService.shutdownNow();
      if (!runnables.isEmpty()) {
        LOG.error(String.format("%d tasks still running. Thread interrupted!", runnables.size()));
      }
      Thread.currentThread().interrupt();
    }
  }

  public static ThreadFactory threadsNamed(final String nameFormat) {
    return new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
  }
}
