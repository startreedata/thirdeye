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
    // FIXME CYRIL - for the moment disable if srtrict namespace separation is enabled
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
    // TODO CYRIL authz maintain a list or a map of namespaces that want events loading? - or this data should be stored in the database, not in the config - need rewrite  
    final long start = System.currentTimeMillis();
    final long end = start + config.getHolidayLoadRange();

    holidayEventsLoader.loadHolidays(start, end, null);
  }
}
