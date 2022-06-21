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

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;

public class ThirdEyeDataSourceContext {

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private MetricConfigManager metricConfigManager;

  /* TODO remove. ThirdEye should not expose internal persistence layer APIs to a plugin. */
  @Deprecated
  private DatasetConfigManager datasetConfigManager;

  private DataSourceDTO dataSourceDTO;

  @Deprecated
  public MetricConfigManager getMetricConfigManager() {
    return metricConfigManager;
  }

  public ThirdEyeDataSourceContext setMetricConfigManager(
      final MetricConfigManager metricConfigManager) {
    this.metricConfigManager = metricConfigManager;
    return this;
  }

  @Deprecated
  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public ThirdEyeDataSourceContext setDatasetConfigManager(
      final DatasetConfigManager datasetConfigManager) {
    this.datasetConfigManager = datasetConfigManager;
    return this;
  }

  public DataSourceDTO getDataSourceDTO() {
    return dataSourceDTO;
  }

  public ThirdEyeDataSourceContext setDataSourceDTO(
      final DataSourceDTO dataSourceDTO) {
    this.dataSourceDTO = dataSourceDTO;
    return this;
  }
}
