/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.content;

import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import java.util.Collection;
import java.util.Map;

/**
 * Defines the notification content interface.
 */
public interface NotificationContent {

  /**
   * Initialize the content formatter
   */
  void init(NotificationContext context);

  /**
   * Generate the template dictionary from the list of anomaly results to render in the template
   */
  Map<String, Object> format(Collection<AnomalyResult> anomalies, SubscriptionGroupDTO subsConfig);

  /**
   * Retrieves the template file (.ftl)
   */
  String getTemplate();

  /**
   * Path to the img which contains the anomaly snapshot
   */
  String getSnaphotPath();

  /**
   * Cleanup any temporary data
   */
  void cleanup();
}
