/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;

/**
 * Wrapper for ThirdEye request with derived metric expressions
 */
public class RequestContainer {

  final ThirdEyeRequest request;
  final MetricExpression expression;

  RequestContainer(ThirdEyeRequest request, MetricExpression expression) {
    this.request = request;
    this.expression = expression;
  }

  public ThirdEyeRequest getRequest() {
    return request;
  }

  public MetricExpression getExpression() {
    return expression;
  }
}
