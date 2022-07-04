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
package ai.startree.thirdeye.detection.anomalydetection.datafilter;

import ai.startree.thirdeye.metric.MetricTimeSeries;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Map;

public class DummyDataFilter extends BaseDataFilter {

  @Override
  public void setParameters(Map<String, String> props) {
  }

  @Override
  public boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap) {
    return true;
  }

  @Override
  public boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap,
      long windowStart,
      long windowEnd) {
    return true;
  }
}
