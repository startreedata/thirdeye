/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

/**
 * The severity of anomaly.
 */
public enum AnomalySeverity {
  // the order of definition follows the severity from highest to lowest
  CRITICAL,
  HIGH,
  MEDIUM,
  LOW,
  DEFAULT
}
