/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class TimeSeriesRequestContainer extends RequestContainer {

  final DateTime start;
  final DateTime end;
  final Period interval;

  public TimeSeriesRequestContainer(ThirdEyeRequest request, List<MetricExpression> expressions,
      DateTime start,
      DateTime end, Period interval) {
    super(request, expressions);
    this.start = start;
    this.end = end;
    this.interval = interval;
  }

  public DateTime getStart() {
    return start;
  }

  public DateTime getEnd() {
    return end;
  }

  public Period getInterval() {
    return interval;
  }
}
