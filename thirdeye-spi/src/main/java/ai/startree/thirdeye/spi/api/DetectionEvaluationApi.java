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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DetectionEvaluationApi {

  private Double mape;
  private DetectionDataApi data;
  private List<AnomalyApi> anomalies = new ArrayList<>();

  public Double getMape() {
    return mape;
  }

  public DetectionEvaluationApi setMape(final Double mape) {
    this.mape = mape;
    return this;
  }

  public DetectionDataApi getData() {
    return data;
  }

  public DetectionEvaluationApi setData(final DetectionDataApi data) {
    this.data = data;
    return this;
  }

  public List<AnomalyApi> getAnomalies() {
    return anomalies;
  }

  public DetectionEvaluationApi setAnomalies(
      final List<AnomalyApi> anomalies) {
    this.anomalies = anomalies;
    return this;
  }
}
