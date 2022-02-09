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

package ai.startree.thirdeye.spi.api;

import java.util.List;
import java.util.Map;

public class EventApi implements ThirdEyeCrudApi<EventApi> {

  private Long id;
  private String name;
  private String metric;
  private String service;
  private String type;
  private long startTime;
  private long endTime;
  private Map<String, List<String>> targetDimensionMap;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public EventApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public EventApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public EventApi setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public String getService() {
    return service;
  }

  public EventApi setService(final String service) {
    this.service = service;
    return this;
  }

  public String getType() {
    return type;
  }

  public EventApi setType(final String type) {
    this.type = type;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public EventApi setStartTime(final long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public EventApi setEndTime(final long endTime) {
    this.endTime = endTime;
    return this;
  }

  public Map<String, List<String>> getTargetDimensionMap() {
    return targetDimensionMap;
  }

  public EventApi setTargetDimensionMap(
      final Map<String, List<String>> targetDimensionMap) {
    this.targetDimensionMap = targetDimensionMap;
    return this;
  }
}
