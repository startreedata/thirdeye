/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import com.google.common.base.MoreObjects;

/**
 * Classes to keep the information that can be used to construct the metric value from Pinot
 * results.
 */
public class ThirdEyeRequestMetricExpressions {

  private final ThirdEyeRequest thirdEyeRequest;

  /**
   * Construct a pair of ThirdEye request and metric expression, which can be used to construct the
   * metric value from
   * Pinot results.
   *
   * @param thirdEyeRequest the ThirdEye request.
   */
  public ThirdEyeRequestMetricExpressions(ThirdEyeRequest thirdEyeRequest) {
    this.thirdEyeRequest = thirdEyeRequest;
  }

  /**
   * Returns the ThirdEye request.
   *
   * @return the ThirdEye request.
   */
  public ThirdEyeRequest getThirdEyeRequest() {
    return thirdEyeRequest;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("thirdEyeRequest", thirdEyeRequest)
        .toString();
  }
}
