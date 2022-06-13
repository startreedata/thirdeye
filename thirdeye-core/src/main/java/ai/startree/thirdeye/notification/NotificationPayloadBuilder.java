/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationPayloadBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPayloadBuilder.class);

  private final NotificationReportBuilder notificationReportBuilder;
  private final NotificationEventsBuilder notificationEventsBuilder;

  @Inject
  public NotificationPayloadBuilder(final NotificationReportBuilder notificationReportBuilder,
      final NotificationEventsBuilder notificationEventsBuilder) {
    this.notificationReportBuilder = notificationReportBuilder;
    this.notificationEventsBuilder = notificationEventsBuilder;
  }

  public NotificationPayloadApi buildNotificationPayload(
      final SubscriptionGroupDTO subscriptionGroup,
      final Set<MergedAnomalyResultDTO> anomalies) {
    final NotificationReportApi report = notificationReportBuilder.buildNotificationReportApi(
        subscriptionGroup,
        anomalies);

    report.setRelatedEvents(notificationEventsBuilder.getRelatedEvents(anomalies));

    return new NotificationPayloadApi()
        .setSubscriptionGroup(ApiBeanMapper.toApi(subscriptionGroup))
        .setReport(report)
        .setAnomalyReports(notificationReportBuilder.buildAnomalyReports(anomalies));
  }
}
