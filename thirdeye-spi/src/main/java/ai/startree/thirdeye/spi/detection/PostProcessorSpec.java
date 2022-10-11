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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;

public class PostProcessorSpec extends AbstractSpec {

  // move to post processor spec
  private Boolean ignore;

  /**
   * Expected to be set during DataFetcherOperator init
   * */
  private DatasetConfigManager datasetConfigManager;

  /**
   * Expected to be set during DataFetcherOperator init.
   */
  private MinMaxTimeLoader minMaxTimeLoader;

  public Boolean getIgnore() {
    return ignore;
  }

  public PostProcessorSpec setIgnore(final Boolean ignore) {
    this.ignore = ignore;
    return this;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public PostProcessorSpec setDatasetConfigManager(
      final DatasetConfigManager datasetConfigManager) {
    this.datasetConfigManager = datasetConfigManager;
    return this;
  }

  public MinMaxTimeLoader getMinMaxTimeLoader() {
    return minMaxTimeLoader;
  }

  public PostProcessorSpec setMinMaxTimeLoader(
      final MinMaxTimeLoader minMaxTimeLoader) {
    this.minMaxTimeLoader = minMaxTimeLoader;
    return this;
  }
}
