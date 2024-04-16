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
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import java.util.concurrent.ExecutorService;

public class ApplicationContext {

  private final DataSourceCache dataSourceCache;
  private final DetectionRegistry detectionRegistry;
  private final PostProcessorRegistry postProcessorRegistry;
  private final EventManager eventManager;
  private final DataSourceManager dataSourceDao;
  private final DatasetConfigManager datasetConfigManager;
  private final ExecutorService subTaskExecutor;
  private final DetectionPipelineConfiguration configuration;
  private final EnumerationItemMaintainer enumerationItemMaintainer;

  public ApplicationContext(final DataSourceCache dataSourceCache,
      final DetectionRegistry detectionRegistry,
      final PostProcessorRegistry postProcessorRegistry,
      final EventManager eventManager,
      final DataSourceManager dataSourceDao, final DatasetConfigManager datasetConfigManager,
      final ExecutorService subTaskExecutor,
      final DetectionPipelineConfiguration detectionPipelineConfiguration,
      final EnumerationItemMaintainer enumerationItemMaintainer) {
    this.dataSourceCache = dataSourceCache;
    this.detectionRegistry = detectionRegistry;
    this.postProcessorRegistry = postProcessorRegistry;
    this.eventManager = eventManager;
    this.dataSourceDao = dataSourceDao;
    this.subTaskExecutor = subTaskExecutor;
    this.enumerationItemMaintainer = enumerationItemMaintainer;
    configuration = detectionPipelineConfiguration;
    this.datasetConfigManager = datasetConfigManager;
  }

  public DataSourceCache getDataSourceCache() {
    return dataSourceCache;
  }

  public DetectionRegistry getDetectionRegistry() {
    return detectionRegistry;
  }

  public PostProcessorRegistry getPostProcessorRegistry() {
    return postProcessorRegistry;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public ExecutorService getSubTaskExecutor() {
    return subTaskExecutor;
  }

  public DetectionPipelineConfiguration getConfiguration() {
    return configuration;
  }

  public EnumerationItemMaintainer getEnumerationItemMaintainer() {
    return enumerationItemMaintainer;
  }

  public DataSourceManager getDataSourceDao() {
    return dataSourceDao;
  }
}
