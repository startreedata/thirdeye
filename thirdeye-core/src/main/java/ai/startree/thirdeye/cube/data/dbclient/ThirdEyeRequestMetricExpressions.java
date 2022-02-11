/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import com.google.common.base.MoreObjects;
import java.util.List;

/**
 * Classes to keep the information that can be used to construct the metric value from Pinot
 * results.
 */
public class ThirdEyeRequestMetricExpressions {

  private final ThirdEyeRequest thirdEyeRequest;
  private final List<MetricExpression> metricExpressions;

  /**
   * Construct a pair of ThirdEye request and metric expression, which can be used to construct the
   * metric value from
   * Pinot results.
   *
   * @param thirdEyeRequest the ThirdEye request.
   * @param metricExpressions the metric expression of the ThirdEye request.
   */
  public ThirdEyeRequestMetricExpressions(ThirdEyeRequest thirdEyeRequest,
      List<MetricExpression> metricExpressions) {
    this.thirdEyeRequest = thirdEyeRequest;
    this.metricExpressions = metricExpressions;
  }

  /**
   * Returns the ThirdEye request.
   *
   * @return the ThirdEye request.
   */
  public ThirdEyeRequest getThirdEyeRequest() {
    return thirdEyeRequest;
  }

  /**
   * Returns the metric expression of the ThirdEye request.
   *
   * @return the metric expression of the ThirdEye request.
   */
  public List<MetricExpression> getMetricExpressions() {
    return metricExpressions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("thirdEyeRequest", thirdEyeRequest)
        .add("metricExpressions", metricExpressions)
        .toString();
  }
}
