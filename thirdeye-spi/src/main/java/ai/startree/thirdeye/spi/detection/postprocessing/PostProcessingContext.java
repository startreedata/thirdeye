/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.spi.detection.postprocessing;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;

public class PostProcessingContext {

  private final DataSourceManager dataSourceManager;
  private final DatasetConfigManager datasetConfigManager;
  private final MinMaxTimeLoader minMaxTimeLoader;
  private final AnomalyManager anomalyManager;
  private final Long alertId;
  private final String namespace;
  private final DetectionPipelineUsage usage;
  private final EnumerationItemDTO enumerationItemDTO;

  public PostProcessingContext(
      final DataSourceManager dataSourceManager, final DatasetConfigManager datasetConfigManager,
      final MinMaxTimeLoader minMaxTimeLoader,
      final AnomalyManager anomalyManager,
      final Long alertId,
      final String namespace, final DetectionPipelineUsage usage,
      final EnumerationItemDTO enumerationItemDTO) {
    this.dataSourceManager = dataSourceManager;
    this.datasetConfigManager = datasetConfigManager;
    this.minMaxTimeLoader = minMaxTimeLoader;
    this.anomalyManager = anomalyManager;
    this.alertId = alertId;
    this.namespace = namespace;
    this.usage = usage;
    this.enumerationItemDTO = enumerationItemDTO;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public MinMaxTimeLoader getMinMaxTimeLoader() {
    return minMaxTimeLoader;
  }

  public AnomalyManager getMergedAnomalyResultManager() {
    return anomalyManager;
  }

  public Long getAlertId() {
    return alertId;
  }

  public DetectionPipelineUsage getUsage() {
    return usage;
  }

  public EnumerationItemDTO getEnumerationItemDTO() {
    return enumerationItemDTO;
  }

  public DataSourceManager getDataSourceManager() {
    return dataSourceManager;
  }

  public String getNamespace() {
    return namespace;
  }
}
