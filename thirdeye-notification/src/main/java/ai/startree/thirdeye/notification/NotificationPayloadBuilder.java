/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
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
      final Set<AnomalyDTO> anomalies) {
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
