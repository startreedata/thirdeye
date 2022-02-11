/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.timeseries;

import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import java.util.List;

public interface TimeSeriesResponseParser {

  List<TimeSeriesRow> parseResponse(ThirdEyeResponse response);
}
