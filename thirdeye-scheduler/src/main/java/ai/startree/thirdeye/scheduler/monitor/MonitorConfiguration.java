/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.scheduler.monitor;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.TimeUnit;

public class MonitorConfiguration {

  private static final int DEFAULT_RETENTION_DAYS = 30;
  private static final int DEFAULT_COMPLETED_JOB_RETENTION_DAYS = 14;
  private static final int DEFAULT_DETECTION_STATUS_RETENTION_DAYS = 7;
  private static final int DEFAULT_RAW_ANOMALY_RETENTION_DAYS = 30;
  private static TimeGranularity DEFAULT_MONITOR_FREQUENCY = new TimeGranularity(1, TimeUnit.DAYS);

  private int defaultRetentionDays = DEFAULT_RETENTION_DAYS;
  private int completedJobRetentionDays = DEFAULT_COMPLETED_JOB_RETENTION_DAYS;
  private int detectionStatusRetentionDays = DEFAULT_DETECTION_STATUS_RETENTION_DAYS;
  private int rawAnomalyRetentionDays = DEFAULT_RAW_ANOMALY_RETENTION_DAYS;
  private TimeGranularity monitorFrequency = DEFAULT_MONITOR_FREQUENCY;

  public int getCompletedJobRetentionDays() {
    return completedJobRetentionDays;
  }

  public void setCompletedJobRetentionDays(int completedJobRetentionDays) {
    this.completedJobRetentionDays = completedJobRetentionDays;
  }

  public int getDefaultRetentionDays() {
    return defaultRetentionDays;
  }

  public void setDefaultRetentionDays(int defaultRetentionDays) {
    this.defaultRetentionDays = defaultRetentionDays;
  }

  public int getDetectionStatusRetentionDays() {
    return detectionStatusRetentionDays;
  }

  public void setDetectionStatusRetentionDays(int detectionStatusRetentionDays) {
    this.detectionStatusRetentionDays = detectionStatusRetentionDays;
  }

  public int getRawAnomalyRetentionDays() {
    return rawAnomalyRetentionDays;
  }

  public void setRawAnomalyRetentionDays(int rawAnomalyRetentionDays) {
    this.rawAnomalyRetentionDays = rawAnomalyRetentionDays;
  }

  public TimeGranularity getMonitorFrequency() {
    return monitorFrequency;
  }

  public void setMonitorFrequency(TimeGranularity monitorFrequency) {
    this.monitorFrequency = monitorFrequency;
  }
}
