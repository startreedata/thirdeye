/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.aspect;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// use this class to controle the value returned by System.currentTimeMillis()
// thread safety has not been thoroughly tested
public class TimeProvider {

  private final static TimeProvider instance = new TimeProvider();

  public static TimeProvider instance() {
    return instance;
  }

  private final AtomicLong currentTimeMillis = new AtomicLong();
  private final AtomicLong nanoTime = new AtomicLong();
  private final AtomicBoolean mockTime = new AtomicBoolean(false);

  public boolean isTimeMockWorking() {
    // setting before the test
    boolean originalMockTime = mockTime.get();
    final long originalCurrentTimeMillis = currentTimeMillis.get();
    final long originalNanoTime = nanoTime.get();

    useMockTime(0);
    final boolean systemMockWorks = System.currentTimeMillis() == 0;
    final boolean dateMockWorks = new Date().getTime() == 0;
    final long nanoTimeBeforeTick = System.nanoTime();
    final int tickTestValue = 20;
    tick(tickTestValue);
    final boolean systemChangeWorks = System.currentTimeMillis() == tickTestValue;
    final boolean dateChangeWorks = new Date().getTime() == tickTestValue;
    final long nanoTimeAfterTick = System.nanoTime();
    final boolean nanoTimeWorks = (nanoTimeAfterTick -  nanoTimeBeforeTick) / 1000_000 == tickTestValue;

    // set back to the setting before the test
    mockTime.set(originalMockTime);
    currentTimeMillis.set(originalCurrentTimeMillis);
    nanoTime.set(originalNanoTime);

    return systemMockWorks && dateMockWorks && systemChangeWorks && dateChangeWorks && nanoTimeWorks;
  }

  public boolean isTimedMocked() {
    return mockTime.get();
  }

  /**
   * Set mock time and enable time mocking
   */
  public synchronized void useMockTime(long currentTime) {
    currentTimeMillis.set(currentTime);
    // could be any value - using same as currentTime to simplify debugging
    nanoTime.set(currentTime * 1000_000);  
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
    nanoTime.addAndGet(tick * 1_000_000);
    return currentTimeMillis.addAndGet(tick);
  }

  public long nanoTime() {
    return nanoTime.get();
  }
}
