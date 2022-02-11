/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.formatter.channels;

import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.notification.content.NotificationContent;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import java.util.Collection;
import java.util.Map;

/**
 * This class formats the content for email alerts.
 */
public class EmailContentFormatter {

  public Map<String, Object> buildTemplateData(final NotificationContext notificationContext,
      final NotificationContent content, final SubscriptionGroupDTO sg,
      final Collection<AnomalyResult> anomalies) {
    final Map<String, Object> templateData = content.format(anomalies, sg);
    templateData.put("dashboardHost", notificationContext.getUiPublicUrl());
    return templateData;
  }
}
