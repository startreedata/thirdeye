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
package ai.startree.thirdeye.datasource.cache;

import java.util.Objects;

public class MetricDataset {

  private String metricName;
  private String dataset;

  public MetricDataset(String metricName, String dataset) {
    this.metricName = metricName;
    this.dataset = dataset;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricName, dataset);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MetricDataset)) {
      return false;
    }
    MetricDataset md = (MetricDataset) o;
    return Objects.equals(metricName, md.getMetricName())
        && Objects.equals(dataset, md.getDataset());
  }
}
