/*
 * Copyright 2024 StarTree Inc
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
import static com.google.common.base.Suppliers.memoizeWithExpiration;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.core.ConfusionMatrix;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// FIXME CYRIL authz move in subpackage 
@Singleton
public class AnomalyMetricsProvider {

  private final AnomalyManager anomalyManager;
  private final AuthorizationManager authorizationManager;

  private Supplier<List<AnomalyFeedback>> anomalyFeedbacksSupplier =
      memoizeWithExpiration(() -> getAnomalyFeedbacks(null),
          METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES);

  @Inject
  public AnomalyMetricsProvider(AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager,
      final MetricRegistry metricRegistry) {
    this.anomalyManager = anomalyManager;
    this.authorizationManager = authorizationManager;
    // fixme cyril authz - across namespace metrics should be defined in the DAO, not in the service  
    Gauge.builder("thirdeye_anomalies",
            memoizeWithExpiration(() -> countTotal(null), METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    Gauge.builder("thirdeye_anomaly_feedbacks",
            memoizeWithExpiration(() -> countFeedbacks(null), METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    final Supplier<ConfusionMatrix> cachedConfusionMatrix = memoizeWithExpiration(
        this::computeConfusionMatrixForAnomalies, METRICS_CACHE_TIMEOUT.toMinutes(),
        TimeUnit.MINUTES);
    Gauge.builder("thirdeye_anomaly_precision",
            () -> cachedConfusionMatrix.get().getPrecision())
        .register(Metrics.globalRegistry);
    Gauge.builder("thirdeye_anomaly_response_rate",
            () -> cachedConfusionMatrix.get().getResponseRate())
        .register(Metrics.globalRegistry);
  }

  private static Predicate notIgnored() {
    return Predicate.EQ("ignored", false);
  }
  
  public AnomalyStatsApi computeAnomalyStats(final ThirdEyePrincipal principal, final Predicate predicate) {
    // FIXME CYRIL may be slow - cache the result? need to cache per (predicate, namespace)
    // FIXME CYRIL add authz - must apply authz independently of the predicate - will need to inject namespace predicate for speed and to prevent noisy neighbours  
    final List<AnomalyFeedback> allFeedbacks = getAnomalyFeedbacks(predicate);
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
    return anomalyManager.countParentAnomalies(finalPredicate);
  }

  private Long countFeedbacks(final Predicate predicate) {
    Predicate finalPredicate = Predicate.NEQ("anomalyFeedbackId", 0);
    if (predicate != null) {
      finalPredicate = Predicate.AND(finalPredicate, predicate);
    }
    return countTotal(finalPredicate);
  }

  private ConfusionMatrix computeConfusionMatrixForAnomalies() {
    final ConfusionMatrix matrix = new ConfusionMatrix();
    // filter to get anomalies without feedback and which are not ignored
    final Predicate unclassified = Predicate.AND(notIgnored(),
        Predicate.EQ("anomalyFeedbackId", 0));
    matrix.addUnclassified((int) anomalyManager.countParentAnomalies(unclassified));

    final List<AnomalyFeedback> allFeedbacks = anomalyFeedbacksSupplier.get();
    final Map<AnomalyFeedbackType, Long> typeMap = aggregateFeedbackTypes(allFeedbacks);
    matrix.addUnclassified(Math.toIntExact(typeMap.get(NO_FEEDBACK)));
    matrix.addFalsePositive(Math.toIntExact(typeMap.get(NOT_ANOMALY)));
    matrix.addTruePositive(Math.toIntExact(typeMap.get(ANOMALY))
        + Math.toIntExact(typeMap.get(ANOMALY_EXPECTED))
        + Math.toIntExact(typeMap.get(ANOMALY_NEW_TREND)));
    return matrix;
  }

  private List<AnomalyFeedback> getAnomalyFeedbacks(final Predicate predicate) {
    Predicate finalPredicate = notIgnored();
    if (predicate != null) {
      finalPredicate = Predicate.AND(finalPredicate, predicate);
    }
    // FIXME CYRIL add authz
    return anomalyManager.findParentAnomaliesWithFeedback(finalPredicate).stream()
        .map(AnomalyDTO::getFeedback)
        .collect(Collectors.toList());
  }

  private Map<AnomalyFeedbackType, Long> aggregateFeedbackTypes(
      final List<AnomalyFeedback> feedbacks) {
    final Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();
    for (final AnomalyFeedbackType type : AnomalyFeedbackType.values()) {
      feedbackStats.put(type, 0L);
    }
    feedbacks.stream()
        .map(AnomalyFeedback::getFeedbackType)
        .forEach(type -> feedbackStats.put(type, feedbackStats.get(type) + 1));
    return feedbackStats;
  }
}
