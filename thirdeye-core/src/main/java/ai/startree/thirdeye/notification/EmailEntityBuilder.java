/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for sending the email alerts
 */
@Singleton
public class EmailEntityBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(EmailEntityBuilder.class);

  private final SmtpConfiguration smtpConfig;
  private final UiConfiguration uiConfig;
  private final AnomalyEmailContentBuilder anomalyEmailContentBuilder;

  @Inject
  public EmailEntityBuilder(final UiConfiguration uiConfig,
      final AnomalyEmailContentBuilder anomalyEmailContentBuilder,
      final NotificationConfiguration notificationConfiguration) {
    this.anomalyEmailContentBuilder = anomalyEmailContentBuilder;
    this.uiConfig = uiConfig;

    smtpConfig = notificationConfiguration.getSmtpConfiguration();
  }

  public Map<String, Object> buildTemplateData(final SubscriptionGroupDTO sg,
      final List<? extends AnomalyResult> anomalyResults) {
    requireNonNull(anomalyResults, "anomalyResults is null");
    checkArgument(anomalyResults.size() > 0, "anomalyResults is empty");

    final List<AnomalyResult> sortedAnomalyResults = new ArrayList<>(anomalyResults);
    sortedAnomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));

    if (Strings.isNullOrEmpty(sg.getFrom())) {
      final String fromAddress = smtpConfig.getUser();
      if (Strings.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("Invalid sender's email");
      }

      // TODO spyne Investigate and remove logic where email send is updating dto object temporarily
      sg.setFrom(fromAddress);
    }

    final Map<String, Object> templateData = anomalyEmailContentBuilder.format(
        sortedAnomalyResults,
        sg);
    templateData.put("dashboardHost", uiConfig.getExternalUrl());
    return templateData;
  }
}
