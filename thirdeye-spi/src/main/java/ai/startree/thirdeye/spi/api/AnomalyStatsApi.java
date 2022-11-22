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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.util.HashMap;
import java.util.Map;

public class AnomalyStatsApi implements ThirdEyeApi{

  private Long totalCount;
  private Long countWithFeedback;
  private Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();

  public Long getTotalCount() {
    return totalCount;
  }

  public AnomalyStatsApi setTotalCount(final Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  public Long getCountWithFeedback() {
    return countWithFeedback;
  }

  public AnomalyStatsApi setCountWithFeedback(final Long countWithFeedback) {
    this.countWithFeedback = countWithFeedback;
    return this;
  }

  public Map<AnomalyFeedbackType, Long> getFeedbackStats() {
    return feedbackStats;
  }

  public AnomalyStatsApi setFeedbackStats(
      final Map<AnomalyFeedbackType, Long> feedbackStats) {
    this.feedbackStats = feedbackStats;
    return this;
  }
}
