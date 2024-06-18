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

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// FIXME CYRIL authz move in subpackage 
@Singleton
public class AnomalyMetricsProvider {

  private final AnomalyManager anomalyManager;
  private final AuthorizationManager authorizationManager;
  
  @Inject
  public AnomalyMetricsProvider(AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    this.anomalyManager = anomalyManager;
    this.authorizationManager = authorizationManager;
  }

  public AnomalyStatsApi computeAnomalyStats(final ThirdEyePrincipal principal,
      final AnomalyFilter filter) {
    // FIXME CYRIL add authz - must apply authz independently of the predicate - will need to inject namespace predicate for speed and to prevent noisy neighbours
    // FIXME CYRIL add authz
    final AnomalyFilter notChildNotIgnoredFilter = filter.copy().setIsIgnored(false).setIsChild(false);
    final AnomalyFilter feedbackFilter = notChildNotIgnoredFilter.copy().setHasFeedback(true);
    final List<AnomalyFeedback> allFeedbacks = anomalyManager.filter(feedbackFilter).stream()
        .map(AnomalyDTO::getFeedback)
        .toList();
    return new AnomalyStatsApi()
        .setTotalCount(anomalyManager.count(notChildNotIgnoredFilter))
        .setCountWithFeedback((long) allFeedbacks.size())
        .setFeedbackStats(feedbackTypesCount(allFeedbacks));
  }

  private static Map<AnomalyFeedbackType, Long> feedbackTypesCount(
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
