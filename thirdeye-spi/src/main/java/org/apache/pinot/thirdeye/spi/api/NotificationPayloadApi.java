package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class NotificationPayloadApi implements ThirdEyeApi {

  private SubscriptionGroupApi subscriptionGroup;
  private List<AnomalyReportApi> anomalyReports;
  private EmailEntityApi emailEntity;

  public SubscriptionGroupApi getSubscriptionGroup() {
    return subscriptionGroup;
  }

  public NotificationPayloadApi setSubscriptionGroup(
      final SubscriptionGroupApi subscriptionGroup) {
    this.subscriptionGroup = subscriptionGroup;
    return this;
  }

  public List<AnomalyReportApi> getAnomalyReports() {
    return anomalyReports;
  }

  public NotificationPayloadApi setAnomalyReports(final List<AnomalyReportApi> anomalyReports) {
    this.anomalyReports = anomalyReports;
    return this;
  }

  public EmailEntityApi getEmailEntity() {
    return emailEntity;
  }

  public NotificationPayloadApi setEmailEntity(
      final EmailEntityApi emailEntity) {
    this.emailEntity = emailEntity;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NotificationPayloadApi that = (NotificationPayloadApi) o;
    return Objects.equal(subscriptionGroup, that.subscriptionGroup)
        && Objects.equal(anomalyReports, that.anomalyReports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(subscriptionGroup, anomalyReports);
  }
}
