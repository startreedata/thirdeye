/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public class AnomalyNotifiedStatus {

  public AnomalyNotifiedStatus() {

  }

  public AnomalyNotifiedStatus(long time, double severity) {
    this.lastNotifyTime = time;
    this.lastNotifySeverity = severity;
  }

  private long lastNotifyTime;
  private double lastNotifySeverity;

  public long getLastNotifyTime() {
    return lastNotifyTime;
  }

  public void setLastNotifyTime(long lastNotifyTime) {
    this.lastNotifyTime = lastNotifyTime;
  }

  public double getLastNotifySeverity() {
    return lastNotifySeverity;
  }

  public void setLastNotifySeverity(double lastNotifySeverity) {
    this.lastNotifySeverity = lastNotifySeverity;
  }
}
