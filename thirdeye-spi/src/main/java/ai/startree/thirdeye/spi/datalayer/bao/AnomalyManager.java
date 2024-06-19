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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AnomalyManager extends AbstractManager<AnomalyDTO> {

  AnomalyDTO findById(Long id);

  AnomalyDTO findParent(AnomalyDTO entity);

  void updateAnomalyFeedback(AnomalyDTO entity);

  AnomalyDTO convertMergeAnomalyDTO2Bean(AnomalyDTO entity);

  List<AnomalyDTO> decorate(List<AnomalyDTO> anomalyDTOList);

  long count(final @NonNull AnomalyFilter filter);

  // fixme cyril authz **EXTREMELY** inefficient - will load entities in memory before counting
  // fixme cyril authz add namespace to AnomalyFilter - requires to put namespace in the index tables - so requires to migrate entities
  default long countWithNamespace(final @NonNull AnomalyFilter filter, final @Nullable String namespace) {
    return filter(filter).stream()
        .filter(e -> Objects.equals(e.namespace(), namespace))
        .count(); 
  }

  List<AnomalyDTO> filter(@NonNull AnomalyFilter anomalyFilter);

  // fixme cyril authz add namespace to AnomalyFilter - requires to put namespace in the index tables
  default List<AnomalyDTO> filterWithNamespace(final @NonNull AnomalyFilter anomalyFilter,
      final @Nullable String namespace) {
    return filter(anomalyFilter).stream()
        .filter(e -> Objects.equals(e.namespace(), namespace))
        .toList();
  }

  default AnomalyStatsApi anomalyStats(final @Nullable String namespace, final AnomalyFilter filter) {
    final AnomalyFilter notChildNotIgnoredFilter = filter.copy().setIsIgnored(false).setIsChild(false);
    final AnomalyFilter feedbackFilter = notChildNotIgnoredFilter.copy().setHasFeedback(true);
    final List<AnomalyFeedback> allFeedbacks =filterWithNamespace(feedbackFilter, namespace)
        .stream()
        .map(AnomalyDTO::getFeedback)
        .toList();
    return new AnomalyStatsApi()
        .setTotalCount(countWithNamespace(notChildNotIgnoredFilter, namespace))
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
