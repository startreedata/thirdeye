package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.util.HashMap;
import java.util.Map;

public class AnomalyStatsApi {

  private Long totalCount;
  private Long countWithFeedback;
  private Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();

  public AnomalyStatsApi() {
    for(AnomalyFeedbackType type : AnomalyFeedbackType.values()) {
      feedbackStats.put(type, 0L);
    }
  }

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

  public void incFeedbackStatCount(AnomalyFeedbackType type) {
    feedbackStats.put(type, feedbackStats.get(type) + 1L);
  }
}
