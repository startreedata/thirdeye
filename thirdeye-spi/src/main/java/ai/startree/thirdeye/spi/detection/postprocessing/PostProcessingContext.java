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
package ai.startree.thirdeye.spi.detection.postprocessing;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;

public class PostProcessingContext {

  private final DatasetConfigManager datasetConfigManager;
  private final MinMaxTimeLoader minMaxTimeLoader;

  public PostProcessingContext(
      final DatasetConfigManager datasetConfigManager,
      final MinMaxTimeLoader minMaxTimeLoader) {
    this.datasetConfigManager = datasetConfigManager;
    this.minMaxTimeLoader = minMaxTimeLoader;
  }

  public DatasetConfigManager getDatasetConfigManager() {
    return datasetConfigManager;
  }

  public MinMaxTimeLoader getMinMaxTimeLoader() {
    return minMaxTimeLoader;
  }
}
