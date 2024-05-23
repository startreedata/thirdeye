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
package ai.startree.thirdeye.spi;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;

public interface Constants {
  Locale DEFAULT_LOCALE = Locale.ENGLISH;

  Chronology DEFAULT_CHRONOLOGY = ISOChronology.getInstanceUTC();
  String UTC_TIMEZONE = "UTC";
  String CRON_TIMEZONE = UTC_TIMEZONE;

  // Auth related constants
  String NO_AUTH_USER = "no-auth-user";
  String AUTH_BEARER = "Bearer";
  String AUTH_BASIC = "Basic";
  String OAUTH_ISSUER = "issuer";
  String OAUTH_JWKS_URI = "jwks_uri";

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
  long NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS = TimeUnit.DAYS.toMillis(1400);

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
  // used for masking. Should be of boolean type or castable to boolean. True corresponds to masked value.
  String COL_MASK = "mask";
  String COL_UPPER_BOUND = "upper_bound";
  String COL_LOWER_BOUND = "lower_bound";
  String COL_ANOMALY = "anomaly";
  String COL_ERROR = "error";
  String COL_DIFF = "diff";

  // constants for event dataframes
  String COL_EVENT_NAME = "event_name";
  String COL_EVENT_START = "event_start";
  String COL_EVENT_END = "event_end";

  String TWO_DECIMALS_FORMAT = "#,###.##";
  String MAX_DECIMALS_FORMAT = "#,###.#####";
  String DECIMALS_FORMAT_TOKEN = "#";

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

  Duration METRICS_CACHE_TIMEOUT = Duration.ofMinutes(10);
  double[] METRICS_TIMER_PERCENTILES = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999};
}
