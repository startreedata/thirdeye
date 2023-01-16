/*
 * Copyright 2023 StarTree Inc
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

public class AnomalyReportApi implements ThirdEyeApi {
  private AnomalyApi anomaly;
  private String url;

  private AnomalyReportDataApi data;

  public AnomalyApi getAnomaly() {
    return anomaly;
  }

  public AnomalyReportApi setAnomaly(final AnomalyApi anomaly) {
    this.anomaly = anomaly;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public AnomalyReportApi setUrl(final String url) {
    this.url = url;
    return this;
  }

  public AnomalyReportDataApi getData() {
    return data;
  }

  public AnomalyReportApi setData(final AnomalyReportDataApi data) {
    this.data = data;
    return this;
  }
}
