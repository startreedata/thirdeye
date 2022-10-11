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

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import java.util.List;
import org.joda.time.Interval;

public class CohortComputationContext {

  private MetricConfigDTO metric;
  private DatasetConfigDTO dataset;
  private ThirdEyeDataSource dataSource;

  private Double threshold;
  private Interval interval;
  private List<String> allDimensions;

  public MetricConfigDTO getMetric() {
    return metric;
  }

  public CohortComputationContext setMetric(final MetricConfigDTO metric) {
    this.metric = metric;
    return this;
  }

  public DatasetConfigDTO getDataset() {
    return dataset;
  }

  public CohortComputationContext setDataset(final DatasetConfigDTO dataset) {
    this.dataset = dataset;
    return this;
  }

  public ThirdEyeDataSource getDataSource() {
    return dataSource;
  }

  public CohortComputationContext setDataSource(final ThirdEyeDataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public Double getThreshold() {
    return threshold;
  }

  public CohortComputationContext setThreshold(final Double threshold) {
    this.threshold = threshold;
    return this;
  }

  public Interval getInterval() {
    return interval;
  }

  public CohortComputationContext setInterval(final Interval interval) {
    this.interval = interval;
    return this;
  }

  public List<String> getAllDimensions() {
    return allDimensions;
  }

  public CohortComputationContext setAllDimensions(final List<String> allDimensions) {
    this.allDimensions = allDimensions;
    return this;
  }
}
