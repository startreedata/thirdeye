/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class TimeSeriesRequestContainer extends RequestContainer {

  final DateTime start;
  final DateTime end;
  final Period granularity;

  public TimeSeriesRequestContainer(ThirdEyeRequest request, MetricExpression expression,
      DateTime start,
      DateTime end, Period granularity) {
    super(request, expression);
    this.start = start;
    this.end = end;
    this.granularity = granularity;
  }

  public DateTime getStart() {
    return start;
  }

  public DateTime getEnd() {
    return end;
  }

  public Period getGranularity() {
    return granularity;
  }
}
