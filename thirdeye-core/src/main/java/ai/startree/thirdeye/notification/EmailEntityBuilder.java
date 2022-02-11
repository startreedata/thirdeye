/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.notification.commons.SmtpConfiguration;
import ai.startree.thirdeye.notification.content.templates.MetricAnomaliesContent;
import ai.startree.thirdeye.notification.formatter.channels.EmailContentFormatter;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the email alerts
 */
@Singleton
public class EmailEntityBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(EmailEntityBuilder.class);

  private final EmailContentFormatter emailContentFormatter;
  private final SmtpConfiguration smtpConfig;
  private final UiConfiguration uiConfig;
  private final MetricAnomaliesContent metricAnomaliesContent;

  @Inject
  public EmailEntityBuilder(final ThirdEyeServerConfiguration configuration,
      final MetricAnomaliesContent metricAnomaliesContent) {
    this.metricAnomaliesContent = metricAnomaliesContent;

    emailContentFormatter = new EmailContentFormatter();
    smtpConfig = configuration.getNotificationConfiguration().getSmtpConfiguration();
    uiConfig = configuration.getUiConfiguration();
  }

  public Map<String, Object> buildTemplateData(final SubscriptionGroupDTO sg,
      final List<AnomalyResult> anomalyResults) {
    requireNonNull(anomalyResults, "anomalyResults is null");
    checkArgument(anomalyResults.size() > 0, "anomalyResults is empty");

    final List<AnomalyResult> sortedAnomalyResults = new ArrayList<>(anomalyResults);
    sortedAnomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    final NotificationContext notificationContext = new NotificationContext()
        .setProperties(new Properties())
        .setUiPublicUrl(uiConfig.getExternalUrl());
    metricAnomaliesContent.init(notificationContext);

    if (Strings.isNullOrEmpty(sg.getFrom())) {
      final String fromAddress = smtpConfig.getUser();
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }

      // TODO spyne Investigate and remove logic where email send is updating dto object temporarily
      sg.setFrom(fromAddress);
    }

    return emailContentFormatter.buildTemplateData(
        notificationContext,
        metricAnomaliesContent,
        sg,
        sortedAnomalyResults);
  }
}
