package ai.startree.thirdeye.spi.api.cube;

import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.api.ThirdEyeApi;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.util.HashMap;
import java.util.Map;

public class AnomalyStatsWrapperApi implements ThirdEyeApi {
  private Map<Long, AnomalyStatsApi> statsByAlerts;
  private Long totalCount;
  private Long countWithFeedback;
  private Map<AnomalyFeedbackType, Long> feedbackStats = new HashMap<>();

  public Map<Long, AnomalyStatsApi> getStatsByAlerts() {
    return statsByAlerts;
  }

  public AnomalyStatsWrapperApi setStatsByAlerts(
      final Map<Long, AnomalyStatsApi> statsByAlerts) {
    this.statsByAlerts = statsByAlerts;
    return this;
  }

  public Long getTotalCount() {
    return totalCount;
  }

  public AnomalyStatsWrapperApi setTotalCount(final Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  public Long getCountWithFeedback() {
    return countWithFeedback;
  }

  public AnomalyStatsWrapperApi setCountWithFeedback(final Long countWithFeedback) {
    this.countWithFeedback = countWithFeedback;
    return this;
  }

  public Map<AnomalyFeedbackType, Long> getFeedbackStats() {
    return feedbackStats;
  }

  public AnomalyStatsWrapperApi setFeedbackStats(
      final Map<AnomalyFeedbackType, Long> feedbackStats) {
    this.feedbackStats = feedbackStats;
    return this;
  }
}
