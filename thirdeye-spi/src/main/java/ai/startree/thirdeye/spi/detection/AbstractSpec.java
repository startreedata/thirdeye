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
package ai.startree.thirdeye.spi.detection;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_INVALID_PARAMS_COMPONENTS;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for component specs
 */
public abstract class AbstractSpec implements Serializable {

  public static final String DEFAULT_TIMESTAMP = "timestamp";
  public static final String DEFAULT_METRIC = "value";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  // do not use timezone. Use the timezone from the detection interval. // todo remove this field - ensure it's removed from the UI first
  @Deprecated
  private String timezone = Constants.DEFAULT_TIMEZONE_STRING;
  private String timestamp = DEFAULT_TIMESTAMP;
  private String metric = DEFAULT_METRIC;
  /**
   * Period in Java ISO8601 standard. Eg 'P1D' or 'PT1H'.
   */
  private @Nullable String monitoringGranularity = null;

  /**
   * Helper for creating spec pojos from Map.class
   *
   * @param properties a map containing parameters
   * @param specClass the POJO class to be serialized into
   * @param <T> Generic Param. Accepts classes which extend this class
   * @return pojo created from properties map
   */
  public static <T extends AbstractSpec> T fromProperties(Map<String, Object> properties,
      Class<T> specClass) {
    try {
    return OBJECT_MAPPER.convertValue(properties, specClass);
    } catch (IllegalArgumentException e) {
      throw new ThirdEyeException(e, ERR_INVALID_PARAMS_COMPONENTS, properties, specClass);
    }
  }

  @Deprecated
  public String getTimezone() {
    return timezone;
  }

  @Deprecated
  public AbstractSpec setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public AbstractSpec setTimestamp(final String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public AbstractSpec setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public @Nullable String getMonitoringGranularity() {
    return monitoringGranularity;
  }

  public AbstractSpec setMonitoringGranularity(final String monitoringGranularity) {
    this.monitoringGranularity = monitoringGranularity;
    return this;
  }
}
