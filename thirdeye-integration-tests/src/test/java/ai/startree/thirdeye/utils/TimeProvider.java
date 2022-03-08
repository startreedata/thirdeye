/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// thread safety has not been thoroughly tested
public class TimeProvider {

  private final static TimeProvider instance = new TimeProvider();

  public static TimeProvider instance() {
    return instance;
  }

  private final AtomicLong currentTimeMillis = new AtomicLong();
  private final AtomicBoolean mockTime = new AtomicBoolean(false);

  public boolean isTimedMocked() {
    return mockTime.get();
  }

  /**
   * Set mock time and enable time mocking
   */
  public synchronized void useMockTime(long currentTime) {
    currentTimeMillis.set(currentTime);
    mockTime.set(true);
  }

  /**
   * Disable time mocking
   */
  public void useSystemTime() {
    mockTime.set(false);
  }

  /**
   * Get mock time value
   */
  public long currentTimeMillis() {
    return currentTimeMillis.get();
  }

  /**
   * Advance mock time and returns it
   */
  public synchronized long tick(long tick) {
    return currentTimeMillis.addAndGet(tick);
  }
}
