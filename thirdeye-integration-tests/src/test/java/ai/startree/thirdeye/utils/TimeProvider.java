/*
 * Copyright 2022 StarTree Inc
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
