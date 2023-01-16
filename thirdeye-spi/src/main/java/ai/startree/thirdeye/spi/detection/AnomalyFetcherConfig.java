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
package ai.startree.thirdeye.spi.detection;

public class AnomalyFetcherConfig {

  private String type;
  private AnomalySource anomalySourceType;
  private String anomalySource;
  private String properties;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public AnomalySource getAnomalySourceType() {
    return anomalySourceType;
  }

  public void setAnomalySourceType(AnomalySource anomalySourceType) {
    this.anomalySourceType = anomalySourceType;
  }

  public String getAnomalySource() {
    return anomalySource;
  }

  public void setAnomalySource(String anomalySource) {
    this.anomalySource = anomalySource;
  }

  public String getProperties() {
    return properties;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }
}
