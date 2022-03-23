/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import java.util.List;

/**
 * Wrapper for ThirdEye request with derived metric expressions
 */
public class RequestContainer {

  final ThirdEyeRequest request;
  final List<MetricExpression> expressions;

  RequestContainer(ThirdEyeRequest request, List<MetricExpression> expressions) {
    this.request = request;
    this.expressions = expressions;
  }

  public ThirdEyeRequest getRequest() {
    return request;
  }

  public List<MetricExpression> getExpressions() {
    return expressions;
  }
}
