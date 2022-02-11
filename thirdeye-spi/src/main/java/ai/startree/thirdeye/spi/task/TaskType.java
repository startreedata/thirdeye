/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.task;

public enum TaskType {
  DETECTION,                 // tasks to detect anomalies
  NOTIFICATION,              // tasks to notify/send alerts to customers regarding anomalies
  ONBOARDING,                // tasks to onboard a new alert
  MONITOR                    // tasks to clean up expired/invalid execution history
}
