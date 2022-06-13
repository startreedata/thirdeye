/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.suppress;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

/**
 * The base alert suppressor whose purpose is to suppress the  actual  alert
 * and ensure no alerts/notifications are sent to the recipients. Alerts may
 * be suppressed or delayed on various occasions to cut down the alert noise
 * especially triggered during deployments, holidays, etc.
 */
public abstract class DetectionAlertSuppressor {

  protected final SubscriptionGroupDTO config;

  public DetectionAlertSuppressor(SubscriptionGroupDTO config) {
    this.config = config;
  }

  public abstract DetectionAlertFilterResult run(DetectionAlertFilterResult result)
      throws Exception;
}
