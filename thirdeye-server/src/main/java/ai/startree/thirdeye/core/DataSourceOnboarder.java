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

package ai.startree.thirdeye.core;

import static ai.startree.thirdeye.util.ResourceUtils.ensure;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DataSourceOnboarder {

  private final DataSourceCache dataSourceCache;
  private final DataSourceManager dataSourceManager;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public DataSourceOnboarder(final DataSourceCache dataSourceCache,
      final DataSourceManager dataSourceManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.dataSourceCache = dataSourceCache;
    this.dataSourceManager = dataSourceManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  public List<DatasetConfigDTO> onboardAll(final ThirdEyeDataSource dataSource) {
    return dataSource.onboardAll();
  }

  public List<DatasetConfigDTO> offboardAll(final String name) {
    ensure(dataSourceManager.findByName(name).size() == 1, "exactly 1 data source expected.");

    final List<DatasetConfigDTO> datasets = datasetConfigManager.findAll()
        .stream()
        .filter(dto -> dto.getDataSource().equals(name))
        .collect(Collectors.toList());

    final Set<String> filtered = datasets.stream()
        .map(DatasetConfigDTO::getDataset)
        .collect(Collectors.toSet());

    for (DatasetConfigDTO dataset : datasets) {
      metricConfigManager.findAll()
          .stream()
          .filter(dto -> filtered.contains(dto.getDataset()))
          .forEach(metricConfigManager::delete);
    }

    datasets.forEach(datasetConfigManager::delete);
    return datasets;
  }
}
