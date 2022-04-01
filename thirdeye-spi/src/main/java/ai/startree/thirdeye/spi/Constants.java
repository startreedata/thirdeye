/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi;

import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;

public interface Constants {

  DateTimeZone DEFAULT_TIMEZONE = DateTimeZone.UTC;
  String DEFAULT_TIMEZONE_STRING = DEFAULT_TIMEZONE.toString();

  String GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME = "detectorComponentName";
  String NO_AUTH_USER = "no-auth-user";
  String AUTH_BEARER = "Bearer";

  // Used in Quartz Scheduler context
  String CTX_INJECTOR = "CTX_INJECTOR";

  // Data Source Related Constants
  int DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE = 50;
  int DEFAULT_LOWER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 100;
  int DEFAULT_UPPER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 8192;

  // timestamp field used to get timestamp in RelationalThirdEyeResponse
  String TIMESTAMP = "timestamp";

  // System property var to check for plugins. Default is "plugins"
  String SYS_PROP_THIRDEYE_PLUGINS_DIR = "thirdEyePluginsDir";

  // Environment var to check for plugins. Default is "plugins"
  String ENV_THIRDEYE_PLUGINS_DIR = "THIRDEYE_PLUGINS_DIR";

  String SCALING_FACTOR = "scalingFactor";

  // Time beyond which we do not want to notify anomalies
  long ANOMALY_NOTIFICATION_LOOKBACK_TIME = TimeUnit.DAYS.toMillis(1400);

  String NOTIFICATIONS_DEFAULT_DATE_PATTERN = "MMM dd, yyyy HH:mm";
  String NOTIFICATIONS_DEFAULT_EVENT_CRAWL_OFFSET = "P2D";
  String NOTIFICATIONS_RAW_VALUE_FORMAT = "%.0f";
  String NOTIFICATIONS_PERCENTAGE_FORMAT = "%.2f %%";

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
}
