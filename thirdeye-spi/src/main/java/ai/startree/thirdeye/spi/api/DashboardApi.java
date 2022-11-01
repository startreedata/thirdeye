package ai.startree.thirdeye.spi.api;

import java.util.List;

public class DashboardApi {
  private Long anomalyCount;
  private Long anomalyCountWithoutFeedback;
  private Long activeAlertsCount;
  private Double precision;
  private Long sgCount;
  private List<Long> anomalyTs;

  public Long getAnomalyCount() {
    return anomalyCount;
  }

  public DashboardApi setAnomalyCount(final Long anomalyCount) {
    this.anomalyCount = anomalyCount;
    return this;
  }

  public Long getAnomalyCountWithoutFeedback() {
    return anomalyCountWithoutFeedback;
  }

  public DashboardApi setAnomalyCountWithoutFeedback(final Long anomalyCountWithoutFeedback) {
    this.anomalyCountWithoutFeedback = anomalyCountWithoutFeedback;
    return this;
  }

  public Long getActiveAlertsCount() {
    return activeAlertsCount;
  }

  public DashboardApi setActiveAlertsCount(final Long activeAlertsCount) {
    this.activeAlertsCount = activeAlertsCount;
    return this;
  }

  public Double getPrecision() {
    return precision;
  }

  public DashboardApi setPrecision(final Double precision) {
    this.precision = precision;
    return this;
  }

  public Long getSgCount() {
    return sgCount;
  }

  public DashboardApi setSgCount(final Long sgCount) {
    this.sgCount = sgCount;
    return this;
  }

  public List<Long> getAnomalyTs() {
    return anomalyTs;
  }

  public DashboardApi setAnomalyTs(final List<Long> anomalyTs) {
    this.anomalyTs = anomalyTs;
    return this;
  }
}
