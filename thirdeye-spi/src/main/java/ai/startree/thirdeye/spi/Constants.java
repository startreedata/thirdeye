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
package ai.startree.thirdeye.spi;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;

public interface Constants {
  Locale DEFAULT_LOCALE = Locale.ENGLISH;

  Chronology DEFAULT_CHRONOLOGY = ISOChronology.getInstanceUTC();
  String CRON_TIMEZONE = "UTC";
  // tz database names that are equivalent to UTC
  Set<String> UTC_LIKE_TIMEZONES = Set.of("Etc/GMT",
      "Etc/GMT+0",
      "Etc/GMT0",
      "GMT",
      "GMT+0",
      "GMT-0",
      "GMT0",
      "Etc/UTC",
      "UTC",
      "Etc/Zulu",
      "Zulu");

  String NO_AUTH_USER = "no-auth-user";
  String AUTH_BEARER = "Bearer";
  String AUTH_BASIC = "Basic";

  // Used in Quartz Scheduler context
  String CTX_INJECTOR = "CTX_INJECTOR";

  // Data Source Related Constants
  int DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE = 25;
  int DEFAULT_LOWER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 100;
  int DEFAULT_UPPER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 8192;

  // System property var to check for plugins. Default is "plugins"
  String SYS_PROP_THIRDEYE_PLUGINS_DIR = "thirdEyePluginsDir";

  // Environment var to check for plugins. Default is "plugins"
  String ENV_THIRDEYE_PLUGINS_DIR = "THIRDEYE_PLUGINS_DIR";

  String SCALING_FACTOR = "scalingFactor";

  // Time beyond which we do not want to notify anomalies
  long ANOMALY_NOTIFICATION_LOOKBACK_TIME = TimeUnit.DAYS.toMillis(1400);

  String NOTIFICATIONS_DEFAULT_DATE_PATTERN = "MMM dd, yyyy HH:mm";
  String NOTIFICATIONS_DEFAULT_EVENT_CRAWL_OFFSET = "P2D";
  String NOTIFICATIONS_PERCENTAGE_FORMAT = "%.2f %%";

  Duration TASK_EXPIRY_DURATION = Duration.ofDays(30);
  int TASK_MAX_DELETES_PER_CLEANUP = 10000;

  /*
   * Dataframe related constants
   */
  // todo cyril timestamp is a reserved keyword in some sql language - use another value for COL_TIME to be able to use it as SQL alias directly
  String COL_TIME = "timestamp";
  String COL_VALUE = "value"; // baseline value
  String COL_CURRENT = "current";
  String COL_UPPER_BOUND = "upper_bound";
  String COL_LOWER_BOUND = "lower_bound";
  String COL_ANOMALY = "anomaly";
  String COL_PATTERN = "pattern";
  String COL_ERROR = "error";
  String COL_DIFF = "diff";
  String COL_DIFF_VIOLATION = "diff_violation";
  String COL_IN_WINDOW = "is_in_window";

  // constants for event dataframes
  String COL_EVENT_NAME = "event_name";
  String COL_EVENT_START = "event_start";
  String COL_EVENT_END = "event_end";

  String TWO_DECIMALS_FORMAT = "#,###.##";
  String MAX_DECIMALS_FORMAT = "#,###.#####";
  String DECIMALS_FORMAT_TOKEN = "#";
  String PROP_DETECTOR_COMPONENT_NAME_DELIMETER = ",";
  // disable minute level cache warm up
  long DETECTION_TASK_MAX_LOOKBACK_WINDOW = TimeUnit.DAYS.toMillis(7);

  enum JobStatus {
    SCHEDULED,
    COMPLETED,
    FAILED,
    TIMEOUT,
    UNKNOWN
  }

  enum SubjectType {
    ALERT,
    METRICS,
    DATASETS
  }

  enum CompareMode {
    WoW, Wo2W, Wo3W, Wo4W
  }

  enum MonitorType {
    UPDATE,
    EXPIRE
  }
}
