/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.detection;

import java.io.Serializable;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Base class for component specs
 */
public abstract class AbstractSpec implements Serializable {

  public static final String DEFAULT_TIMEZONE = "America/Los_Angeles";
  public static final String DEFAULT_TIMESTAMP = "timestamp";
  public static final String DEFAULT_METRIC = "value";

  private String timezone = DEFAULT_TIMEZONE;
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
    // don't reuse model mapper instance. It caches typeMaps and will result in unexpected mappings
    ModelMapper modelMapper = new ModelMapper();
    // use strict mapping to ensure no mismatches or ambiguity occurs
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    return modelMapper.map(properties, specClass);
  }

  public String getTimezone() {
    return timezone;
  }

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
