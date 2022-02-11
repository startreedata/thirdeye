/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import org.joda.time.Instant;

/**
 * The model maintenance flow. This flow re-tunes the detection model automatically if the the
 * model's performance is bad.
 */
public interface ModelMaintenanceFlow {

  /**
   * Maintain the detection model
   *
   * @param detectionConfig the detection config to maintain
   * @param timestamp the time stamp of this maintenance
   * @return the maintained detection config
   */
  AlertDTO maintain(AlertDTO detectionConfig, Instant timestamp);
}
