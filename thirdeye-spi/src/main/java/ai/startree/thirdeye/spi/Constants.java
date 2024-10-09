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

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.json.ApiTemplatableDeserializer;
import ai.startree.thirdeye.spi.json.ApiTemplatableSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.joda.time.Chronology;
import org.joda.time.chrono.ISOChronology;

public interface Constants {
  // serializer/deserializer obtained by reflection are cached in the object mapper. 
  // So it's strongly recommended to use this shared ObjectMapper if no custom ser/deser logic is required.
  // unless you want an ObjectMapper to be GCed because you are sure you will have to ser/deser a type of value only once
  ObjectMapper VANILLA_OBJECT_MAPPER = new ObjectMapper();

  /**
   * ThirdEye implements a custom (de)serialization to simulate the Union type for {@link Templatable}
   * fields.
   * See {@link ApiTemplatableSerializer} and {@link ApiTemplatableDeserializer}
   *
   * In most json exchange context (API level, API json reading/writing, persistence level), you should use  {@link
   * #TEMPLATABLE_OBJECT_MAPPER} to get an ObjectMapper.
   * If you need a jackson.databind.Module with the ThirdEye specific (de)serializations, use this {@link
   * #TEMPLATABLE}.
   *
   * If you need a Vanilla Object Mapper, use {@link ai.startree.thirdeye.spi.Constants#VANILLA_OBJECT_MAPPER}.
   */
  Module TEMPLATABLE = new SimpleModule()
      .addSerializer(Templatable.class, new ApiTemplatableSerializer())
      .addDeserializer(Templatable.class, new ApiTemplatableDeserializer());
  ObjectMapper TEMPLATABLE_OBJECT_MAPPER = new ObjectMapper()
      .registerModule(TEMPLATABLE)
      .registerModule(new JodaModule());
  
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
  int TASK_MAX_DELETES_PER_CLEANUP = 5000;

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

  DecimalFormat TWO_DIGITS_FORMATTER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));

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
  
  String NAMESPACE_HTTP_HEADER = "namespace";
  String NAMESPACE_SECURITY = "namespace";
}
