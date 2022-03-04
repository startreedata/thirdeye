/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.utils;

public class TimeProvider {
  private final static TimeProvider instance = new TimeProvider();

  public static TimeProvider instance() {
    return instance;
  }

  private long currentTimeMillis;
  private boolean mockTime = false;

  public boolean isTimedMocked() {
    return mockTime;
  }

  public void useMockTime(long currentTime) {
    currentTimeMillis = currentTime;
    mockTime = true;
  }

  public void useSystemTime() {
    mockTime = false;
  }

  public long currentTimeMillis() {
    return currentTimeMillis;
  }

  public void tick(long tick) {
    currentTimeMillis += tick;
  }
}
