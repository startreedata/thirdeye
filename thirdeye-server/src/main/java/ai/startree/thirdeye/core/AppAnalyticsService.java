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
package ai.startree.thirdeye.core;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AppAnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AppAnalyticsService.class);

  private final AlertManager alertManager;
  private final AlertTemplateRenderer renderer;
  private final MergedAnomalyResultManager anomalyManager;

  @Inject
  public AppAnalyticsService(final AlertManager alertManager,
      final AlertTemplateRenderer renderer,
      final MergedAnomalyResultManager anomalyManager,
      final MetricRegistry metricRegistry) {
    this.alertManager = alertManager;
    this.anomalyManager = anomalyManager;
    this.renderer = renderer;

    metricRegistry.register("nMonitoredMetrics", new CachedGauge<Integer>(5, TimeUnit.MINUTES) {
      @Override
      protected Integer loadValue() {
        return uniqueMonitoredMetricsCount();
      }
    });
    metricRegistry.register("anomalyPrecision", new CachedGauge<Double>(1, TimeUnit.HOURS) {
      @Override
      protected Double loadValue() {
        return computeConfusionMatrixForAnomalies().getPrecision();
      }
    });
    metricRegistry.register("anomalyResponseRate", new CachedGauge<Double>(1, TimeUnit.HOURS) {
      @Override
      protected Double loadValue() {
        return computeConfusionMatrixForAnomalies().getResponseRate();
      }
    });
  }

  public Integer uniqueMonitoredMetricsCount() {
    return getUniqueMonitoredMetrics().size();
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
    } catch (final IOException | ClassNotFoundException e) {
      log.warn(String.format("Trouble while rendering alert, %s. id : %d", alertDTO.getName(), alertDTO.getId()), e);
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
    matrix.addUnclassified((int) anomalyManager.countParentAnomaliesWithoutFeedback());
    final List<AnomalyFeedback> feedbacks = getAnomalyFeedbacks();
    for (final AnomalyFeedback feedback : feedbacks) {
      final AnomalyFeedbackType type = feedback.getFeedbackType();
      switch (type) {
        case NO_FEEDBACK:
          matrix.incUnclassified();
          break;
        case NOT_ANOMALY:
          matrix.incFalsePositive();
          break;
        case ANOMALY:
        case ANOMALY_EXPECTED:
        case ANOMALY_NEW_TREND:
          matrix.incTruePositive();
          break;
        default:
          log.error("Unsupported feedback type: {}", type);
      }
    }
    return matrix;
  }

  private List<AnomalyFeedback> getAnomalyFeedbacks() {
    return anomalyManager.findParentAnomaliesWithFeedback().stream()
        .map(MergedAnomalyResultDTO::getFeedback)
        .collect(Collectors.toList());
  }

  public AnomalyStatsApi computeAnomalyStats() {
    List<AnomalyFeedback> feedbacks = getAnomalyFeedbacks();
    Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();
    for(AnomalyFeedbackType type : AnomalyFeedbackType.values()) {
      feedbackStats.put(type, 0L);
    }
    feedbacks.forEach(feedback -> {
      AnomalyFeedbackType type = feedback.getFeedbackType();
      long count = feedbackStats.get(type);
      feedbackStats.put(type, count+1);
    });
    return new AnomalyStatsApi()
        .setTotalCount(anomalyManager.countParentAnomalies())
        .setCountWithFeedback((long) feedbacks.size())
        .setFeedbackStats(feedbackStats);
  }
}
