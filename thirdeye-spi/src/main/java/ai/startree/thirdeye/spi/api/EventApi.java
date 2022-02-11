/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
