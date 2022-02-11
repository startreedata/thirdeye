/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.filter;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterNotification;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.annotation.AlertFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

/**
 * The detection alert filter that sends the anomaly email to all recipients
 */
@AlertFilter(type = "DEFAULT_ALERTER_PIPELINE")
public class ToAllRecipientsDetectionAlertFilter extends StatefulDetectionAlertFilter {

  public static final String PROP_RECIPIENTS = "recipients";
  public static final String PROP_TO = "to";
  public static final String PROP_CC = "cc";
  public static final String PROP_BCC = "bcc";
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";

  final Map<String, List<String>> recipients;
  final List<Long> alertIds;

  public ToAllRecipientsDetectionAlertFilter(final DataProvider provider,
      final SubscriptionGroupDTO config,
      final long endTime,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(provider, config, endTime, mergedAnomalyResultManager,
        detectionConfigManager);

    final Map<String, Object> properties = this.config.getProperties();
    recipients = ConfigUtils.getMap(properties.get(PROP_RECIPIENTS));
    alertIds = ConfigUtils.getLongs(properties.get(PROP_DETECTION_CONFIG_IDS));
  }

  @Override
  public DetectionAlertFilterResult run() {
    final DetectionAlertFilterResult result = new DetectionAlertFilterResult();

    // Fetch all the anomalies to be notified to the recipients
    final Set<MergedAnomalyResultDTO> anomalies = filter(makeVectorClocks(alertIds));

    // Handle legacy recipients yaml syntax
    if (SubscriptionUtils.isEmptyEmailRecipients(config) && CollectionUtils
        .isNotEmpty(recipients.get(PROP_TO))) {
      // recipients are configured using the older syntax
      config.setNotificationSchemes(generateNotificationSchemeProps(
          config,
          recipients.get(PROP_TO),
          recipients.get(PROP_CC),
          recipients.get(PROP_BCC)));
    }

    return result.addMapping(new DetectionAlertFilterNotification(config), anomalies);
  }
}
