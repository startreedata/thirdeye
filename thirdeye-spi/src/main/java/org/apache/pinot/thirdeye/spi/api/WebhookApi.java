package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class WebhookApi implements ThirdEyeApi {
  private SubscriptionGroupApi subscriptionGroup;
  private List<AnomalyReportApi> anomalyReports;

  public SubscriptionGroupApi getSubscriptionGroup() {
    return subscriptionGroup;
  }

  public WebhookApi setSubscriptionGroup(
      final SubscriptionGroupApi subscriptionGroup) {
    this.subscriptionGroup = subscriptionGroup;
    return this;
  }

  public List<AnomalyReportApi> getAnomalyReports() {
    return anomalyReports;
  }

  public WebhookApi setAnomalyReports(final List<AnomalyReportApi> anomalyReports) {
    this.anomalyReports = anomalyReports;
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
    final WebhookApi that = (WebhookApi) o;
    return Objects.equal(subscriptionGroup, that.subscriptionGroup)
        && Objects.equal(anomalyReports, that.anomalyReports);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(subscriptionGroup, anomalyReports);
  }

}
