/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.monitor;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.TimeUnit;

public class MonitorConstants {

  public enum MonitorType {
    UPDATE,
    EXPIRE
  }

  public static int DEFAULT_RETENTION_DAYS = 30;
  public static int DEFAULT_COMPLETED_JOB_RETENTION_DAYS = 14;
  public static int DEFAULT_DETECTION_STATUS_RETENTION_DAYS = 7;
  public static int DEFAULT_RAW_ANOMALY_RETENTION_DAYS = 30;
  public static TimeGranularity DEFAULT_MONITOR_FREQUENCY = new TimeGranularity(1, TimeUnit.DAYS);
}
