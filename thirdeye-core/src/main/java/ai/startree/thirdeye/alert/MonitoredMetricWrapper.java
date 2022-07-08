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
package ai.startree.thirdeye.alert;

import com.google.common.base.Objects;

public class MonitoredMetricWrapper {
  private String datasource;
  private String dataset;
  private String metric;

  public String getDatasource() {
    return datasource;
  }

  public MonitoredMetricWrapper setDatasource(final String datasource) {
    this.datasource = datasource;
    return this;
  }

  public String getDataset() {
    return dataset;
  }

  public MonitoredMetricWrapper setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public MonitoredMetricWrapper setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MonitoredMetricWrapper that = (MonitoredMetricWrapper) o;
    return Objects.equal(getDatasource(), that.getDatasource())
        && Objects.equal(getDataset(), that.getDataset())
        && Objects.equal(getMetric(), that.getMetric());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getDatasource(), getDataset(), getMetric());
  }
}
