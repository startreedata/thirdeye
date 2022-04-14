/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import org.joda.time.Period;

public class TimeSeriesRequestContainer extends RequestContainer {

  private final Period granularity;

  public TimeSeriesRequestContainer(ThirdEyeRequest request, MetricExpression expression, Period granularity) {
    super(request, expression);
    this.granularity = granularity;
  }

  public Period getGranularity() {
    return granularity;
  }
}
