package org.apache.pinot.thirdeye;

import java.util.concurrent.TimeUnit;

public interface CoreConstants {

  String TWO_DECIMALS_FORMAT = "#,###.##";
  String MAX_DECIMALS_FORMAT = "#,###.#####";
  String DECIMALS_FORMAT_TOKEN = "#";
  String PROP_DETECTOR_COMPONENT_NAME_DELIMETER = ",";

  // How much data to prefetch to warm up the cache
  long DEFAULT_CACHING_PERIOD_LOOKBACK = -1;
  long CACHING_PERIOD_LOOKBACK_DAILY = TimeUnit.DAYS.toMillis(90);
  long CACHING_PERIOD_LOOKBACK_HOURLY = TimeUnit.DAYS.toMillis(60);

  // disable minute level cache warm up
  long CACHING_PERIOD_LOOKBACK_MINUTELY = -1;
  long DETECTION_TASK_MAX_LOOKBACK_WINDOW = TimeUnit.DAYS.toMillis(7);

  // default onboarding replay period
  // todo cyril made bigger for the product - do better: propose rerun on custom timeframe in the ui
  // also this should depend on the granularity
  long ONBOARDING_REPLAY_LOOKBACK = TimeUnit.DAYS.toMillis(60);

  // TODO suvodeep remove this. Should be in config somewhere.
  String DATA_SOURCES_CONFIG_YML = "file:config/data-sources/data-sources-config.yml";
}
