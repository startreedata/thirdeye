/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
