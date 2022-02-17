/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.detection.TimeSpec;
import java.util.List;
import java.util.Map;

/**
 * The result of calling {@link ThirdEyeDataSource#execute(ThirdEyeRequest)}.
 */
public interface ThirdEyeResponse {

  List<MetricFunction> getMetricFunctions();

  int getNumRows();

  ThirdEyeResponseRow getRow(int rowId);

  int getNumRowsFor(MetricFunction metricFunction);

  // TODO make new API methods to make it clearer how to retrieve metric values vs dimension values,
  // etc. These are all stored in the same map right now.
  Map<String, String> getRow(MetricFunction metricFunction, int rowId);

  ThirdEyeRequest getRequest();

  TimeSpec getDataTimeSpec();

  List<String> getGroupKeyColumns();
}
