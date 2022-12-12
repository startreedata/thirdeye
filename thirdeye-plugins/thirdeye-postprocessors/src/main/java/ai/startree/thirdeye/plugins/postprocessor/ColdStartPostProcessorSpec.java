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
package ai.startree.thirdeye.plugins.postprocessor;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;

public class ColdStartPostProcessorSpec {

  private Boolean ignore;

  /**
   * Cold start period in ISO-8601 format eg P1D.
   * Used with monitoringGranularity to compute lookback in steps.
   */
  private String coldStartPeriod;

  private String tableName;

  /**
   * Expected to be set by the factory
   * */
  private DatasetConfigManager datasetConfigManager;

  /**
   * Expected to be set by the factory
   */
  private MinMaxTimeLoader minMaxTimeLoader;

  public String getColdStartPeriod() {
    return coldStartPeriod;
  }

  public ColdStartPostProcessorSpec setColdStartPeriod(final String coldStartPeriod) {
    this.coldStartPeriod = coldStartPeriod;
    return this;
  }

  public String getTableName() {
    return tableName;
  }

  public ColdStartPostProcessorSpec setTableName(final String tableName) {
    this.tableName = tableName;
    return this;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public ColdStartPostProcessorSpec setDatasetConfigManager(
      final DatasetConfigManager datasetConfigManager) {
    this.datasetConfigManager = datasetConfigManager;
    return this;
  }

  public MinMaxTimeLoader getMinMaxTimeLoader() {
    return minMaxTimeLoader;
  }

  public ColdStartPostProcessorSpec setMinMaxTimeLoader(
      final MinMaxTimeLoader minMaxTimeLoader) {
    this.minMaxTimeLoader = minMaxTimeLoader;
    return this;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public ColdStartPostProcessorSpec setIgnore(final Boolean ignore) {
    this.ignore = ignore;
    return this;
  }
}
