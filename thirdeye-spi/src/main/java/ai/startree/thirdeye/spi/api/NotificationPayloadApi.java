/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class NotificationPayloadApi implements ThirdEyeApi {

  private SubscriptionGroupApi subscriptionGroup;
  private List<AnomalyReportApi> anomalyReports;

  // TODO spyne remove email specific parameters and generify
  private EmailRecipientsApi emailRecipients;

  // TODO spyne remove email specific parameters. Introduce pojo and generify.
  private Map<String, Object> emailTemplateData;

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

  public EmailRecipientsApi getEmailRecipients() {
    return emailRecipients;
  }

  public NotificationPayloadApi setEmailRecipients(final EmailRecipientsApi emailRecipients) {
    this.emailRecipients = emailRecipients;
    return this;
  }

  public Map<String, Object> getEmailTemplateData() {
    return emailTemplateData;
  }

  public NotificationPayloadApi setEmailTemplateData(
      final Map<String, Object> emailTemplateData) {
    this.emailTemplateData = emailTemplateData;
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
