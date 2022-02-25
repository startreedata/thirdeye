/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.EmailRecipientsApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationPayloadBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPayloadBuilder.class);
  private static final String ANOMALY_DASHBOARD_PREFIX = "anomalies/view/id/";

  private final SmtpConfiguration smtpConfig;
  private final UiConfiguration uiConfiguration;
  private final NotificationContentBuilder notificationContentBuilder;

  @Inject
  public NotificationPayloadBuilder(
      final SmtpConfiguration smtpConfig,
      final UiConfiguration uiConfiguration,
      final NotificationContentBuilder notificationContentBuilder) {
    this.smtpConfig = smtpConfig;
    this.uiConfiguration = uiConfiguration;
    this.notificationContentBuilder = notificationContentBuilder;
  }

  public NotificationPayloadApi buildNotificationPayload(
      final SubscriptionGroupDTO subscriptionGroup,
      final Set<MergedAnomalyResultDTO> anomalies) {
    final List<MergedAnomalyResultDTO> anomalyResults = new ArrayList<>(anomalies);
    anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    final Map<String, Object> templateData = notificationContentBuilder.format(anomalies);

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
        .setAnomalyReports(toAnomalyReports(anomalyResults))
        .setEmailTemplateData(templateData)
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

  private List<AnomalyReportApi> toAnomalyReports(final List<MergedAnomalyResultDTO> anomalies) {
    return anomalies.stream()
        .map(dto -> new AnomalyReportApi().setAnomaly(ApiBeanMapper.toApi(dto)))
        .map(r -> r.setUrl(getDashboardUrl(r.getAnomaly().getId())))
        .collect(Collectors.toList());
  }

  private String getDashboardUrl(final Long id) {
    String extUrl = uiConfiguration.getExternalUrl();
    if (!extUrl.matches(".*/")) {
      extUrl += "/";
    }
    return String.format("%s%s%s", extUrl, ANOMALY_DASHBOARD_PREFIX, id);
  }
}
