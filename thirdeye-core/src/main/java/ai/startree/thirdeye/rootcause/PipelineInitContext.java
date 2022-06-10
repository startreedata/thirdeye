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
package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import java.util.Map;
import java.util.Set;

public class PipelineInitContext {

  private String outputName;
  private Set<String> inputNames;
  private Map<String, Object> properties;
  private MetricConfigManager metricConfigManager;
  private DatasetConfigManager datasetConfigManager;
  private DataSourceCache dataSourceCache;
  private ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private EntityToEntityMappingManager entityToEntityMappingManager;
  private EventManager eventManager;
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  public String getOutputName() {
    return outputName;
  }

  public PipelineInitContext setOutputName(final String outputName) {
    this.outputName = outputName;
    return this;
  }

  public Set<String> getInputNames() {
    return inputNames;
  }

  public PipelineInitContext setInputNames(final Set<String> inputNames) {
    this.inputNames = inputNames;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public PipelineInitContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public MetricConfigManager getMetricConfigManager() {
    return metricConfigManager;
  }

  public PipelineInitContext setMetricConfigManager(
      final MetricConfigManager metricConfigManager) {
    this.metricConfigManager = metricConfigManager;
    return this;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public PipelineInitContext setDatasetConfigManager(
      final DatasetConfigManager datasetConfigManager) {
    this.datasetConfigManager = datasetConfigManager;
    return this;
  }

  public DataSourceCache getDataSourceCache() {
    return dataSourceCache;
  }

  public PipelineInitContext setDataSourceCache(
      final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    return this;
  }

  public ThirdEyeCacheRegistry getThirdEyeCacheRegistry() {
    return thirdEyeCacheRegistry;
  }

  public PipelineInitContext setThirdEyeCacheRegistry(
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    return this;
  }

  public EntityToEntityMappingManager getEntityToEntityMappingManager() {
    return entityToEntityMappingManager;
  }

  public PipelineInitContext setEntityToEntityMappingManager(
      final EntityToEntityMappingManager entityToEntityMappingManager) {
    this.entityToEntityMappingManager = entityToEntityMappingManager;
    return this;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

  public PipelineInitContext setEventManager(
      final EventManager eventManager) {
    this.eventManager = eventManager;
    return this;
  }

  public MergedAnomalyResultManager getMergedAnomalyResultManager() {
    return mergedAnomalyResultManager;
  }

  public PipelineInitContext setMergedAnomalyResultManager(
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    return this;
  }
}
