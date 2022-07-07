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
package ai.startree.thirdeye.plugins.detectors;

public class HttpDetectorApi {

  private Long startMillis;
  private Long endMillis;
  private RemoteHttpDetectorSpec spec;
  private DataFrameApi dataframe;

  public Long getStartMillis() {
    return startMillis;
  }

  public HttpDetectorApi setStartMillis(final Long startMillis) {
    this.startMillis = startMillis;
    return this;
  }

  public Long getEndMillis() {
    return endMillis;
  }

  public HttpDetectorApi setEndMillis(final Long endMillis) {
    this.endMillis = endMillis;
    return this;
  }

  public RemoteHttpDetectorSpec getSpec() {
    return spec;
  }

  public HttpDetectorApi setSpec(
      final RemoteHttpDetectorSpec spec) {
    this.spec = spec;
    return this;
  }

  public DataFrameApi getDataframe() {
    return dataframe;
  }

  public HttpDetectorApi setDataframe(
      final DataFrameApi dataframe) {
    this.dataframe = dataframe;
    return this;
  }
}
