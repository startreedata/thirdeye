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
package ai.startree.thirdeye.scheduler.events;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The type Holiday events loader, which loads the holiday events from Google Calendar periodically.
 */
@Singleton
public class HolidayEventsLoaderScheduler implements Runnable {
  
  private final HolidayEventsLoaderConfiguration config;
  /**
   * Calendar Api private key path
   */
  private final ScheduledExecutorService scheduledExecutorService;
  private final HolidayEventsLoader holidayEventsLoader;

  @Inject
  public HolidayEventsLoaderScheduler(
      final HolidayEventsLoaderConfiguration config,
      final HolidayEventsLoader holidayEventsLoader) {
    this.config = config;
    this.holidayEventsLoader = holidayEventsLoader;
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat(
            "holiday-events-loader-%d").build());
  }

  public void start() {
    scheduledExecutorService
        .scheduleAtFixedRate(this, 0, config.getRunFrequency(), TimeUnit.DAYS);
  }

  public void shutdown() {
    scheduledExecutorService.shutdown();
  }

  /**
   * Fetch holidays and save to ThirdEye database.
   */
  public void run() {
    final long start = System.currentTimeMillis();
    final long end = start + config.getHolidayLoadRange();
    // TODO CYRIL authz - namespacing is not implemented for this feature - events will be created in the unset namespace
    //   if strict namespacing is enabled, these events will not be available to any workspace
    //   a rewrite is required to make this feature compatible with namespaces - best would be to maintain a google account and a config in the db, not in the app configuration 
    holidayEventsLoader.loadHolidays(start, end, null);
  }
}
