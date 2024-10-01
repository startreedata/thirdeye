/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

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

  public NotificationPayloadApi build(final NotificationTaskFilterResult result) {
    final SubscriptionGroupDTO subscriptionGroup = result.getSubscriptionGroup();
    final Set<AnomalyDTO> anomalies = optional(result.getAnomalies())
        .orElse(Set.of());
    final Set<AnomalyDTO> completedAnomalies = optional(result.getCompletedAnomalies())
        .orElse(Set.of());

    if (anomalies.isEmpty() && completedAnomalies.isEmpty()) {
      /* No notification required. Do not generate a report */
      return null;
    }

    final NotificationReportApi report = notificationReportBuilder
        .buildNotificationReportApi(subscriptionGroup, anomalies)
        .setRelatedEvents(notificationEventsBuilder.getRelatedEvents(anomalies));

    return new NotificationPayloadApi()
        .setSubscriptionGroup(ApiBeanMapper.toApi(subscriptionGroup))
        .setReport(report)
        .setAnomalyReports(notificationReportBuilder.toSortedAnomalyReports(anomalies))
        .setCompletedAnomalyReports(notificationReportBuilder
            .toSortedAnomalyReports(result.getCompletedAnomalies()));
  }
}
