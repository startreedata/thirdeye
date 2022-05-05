/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.util.CacheUtils;

/**
 * Class used to hold data needed to make a request to centralized cache.
 * Can be derived from ThirdEyeRequest, but it's meant to save some developer
 * effort and abstract away some of the code that needs to be written.
 */
public class ThirdEyeCacheRequest {

  ThirdEyeRequest request;
  private final String metricUrn;
  private final String dimensionKey;

  public ThirdEyeCacheRequest(ThirdEyeRequest request) {
    this.request = request;
    this.metricUrn = MetricEntity.fromMetric(request.getFilterSet().asMap(), this.getMetricId())
        .getUrn();
    this.dimensionKey = CacheUtils.hashMetricUrn(this.metricUrn);
  }

  /**
   * shorthand to create a ThirdEyeCacheRequest from a ThirdEyeRequest
   *
   * @param request ThirdEyeRequest
   * @return ThirdEyeCacheRequest
   */
  public static ThirdEyeCacheRequest from(ThirdEyeRequest request) {
    return new ThirdEyeCacheRequest(request);
  }

  public ThirdEyeRequest getRequest() {
    return request;
  }

  public long getMetricId() {
    return request.getMetricFunction().getMetricId();
  }

  public String getMetricUrn() {
    return metricUrn;
  }

  public long getStartTimeInclusive() {
    return request.getStartTimeInclusive().getMillis();
  }

  public long getEndTimeExclusive() {
    return request.getEndTimeExclusive().getMillis();
  }

  public String getDimensionKey() {
    return dimensionKey;
  }
}
