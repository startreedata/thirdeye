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
package ai.startree.thirdeye.plugins.detection.components.detectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class DataFrameApi {

  private Map<String, List<Serializable>> seriesMap;

  public Map<String, List<Serializable>> getSeriesMap() {
    return seriesMap;
  }

  public DataFrameApi setSeriesMap(
      final Map<String, List<Serializable>> seriesMap) {
    this.seriesMap = seriesMap;
    return this;
  }
}
