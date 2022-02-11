/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.EmailEntityApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationPayloadBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPayloadBuilder.class);
  private static final String ANOMALY_DASHBOARD_PREFIX = "anomalies/view/id/";

  private final UiConfiguration uiConfiguration;
  private final EmailEntityBuilder emailEntityBuilder;

  @Inject
  public NotificationPayloadBuilder(
      final UiConfiguration uiConfiguration,
      final EmailEntityBuilder emailEntityBuilder) {
    this.uiConfiguration = uiConfiguration;
    this.emailEntityBuilder = emailEntityBuilder;
  }

  public NotificationPayloadApi buildNotificationPayload(
      final SubscriptionGroupDTO subscriptionGroup,
      final Set<MergedAnomalyResultDTO> anomalies) {
    final List<MergedAnomalyResultDTO> anomalyResults = new ArrayList<>(anomalies);
    anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    final EmailEntityApi emailEntity = emailEntityBuilder.buildEmailEntity(subscriptionGroup,
        new ArrayList<>(anomalies));

    return new NotificationPayloadApi()
        .setSubscriptionGroup(ApiBeanMapper.toApi(subscriptionGroup))
        .setAnomalyReports(toAnomalyReports(anomalyResults))
        .setEmailEntity(emailEntity);
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
