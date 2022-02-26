/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.EmailRecipientsApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationPayloadBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPayloadBuilder.class);

  private final SmtpConfiguration smtpConfig;
  private final NotificationContentBuilder notificationContentBuilder;

  @Inject
  public NotificationPayloadBuilder(
      final SmtpConfiguration smtpConfig,
      final NotificationContentBuilder notificationContentBuilder) {
    this.smtpConfig = smtpConfig;
    this.notificationContentBuilder = notificationContentBuilder;
  }

  public NotificationPayloadApi buildNotificationPayload(
      final SubscriptionGroupDTO subscriptionGroup,
      final Set<MergedAnomalyResultDTO> anomalies) {
    final EmailSchemeDto emailScheme = subscriptionGroup.getNotificationSchemes().getEmailScheme();
    final EmailRecipientsApi recipients = emailScheme == null ? null : new EmailRecipientsApi(
        emailScheme.getTo(),
        emailScheme.getCc(),
        emailScheme.getBcc()
    ).setFrom(getFromAddress(subscriptionGroup));

    final NotificationReportApi report = notificationContentBuilder.buildNotificationReportApi(
        subscriptionGroup,
        anomalies);

    report.setRelatedEvents(notificationContentBuilder.getRelatedEvents(anomalies));

    return new NotificationPayloadApi()
        .setSubscriptionGroup(ApiBeanMapper.toApi(subscriptionGroup))
        .setReport(report)
        .setAnomalyReports(notificationContentBuilder.buildAnomalyReports(anomalies))
        .setEmailRecipients(recipients);
  }

  private String getFromAddress(final SubscriptionGroupDTO subscriptionGroup) {
    if (Strings.isNullOrEmpty(subscriptionGroup.getFrom())) {
      final String fromAddress = smtpConfig.getUser();
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }
    }

    return subscriptionGroup.getFrom();
  }
}
