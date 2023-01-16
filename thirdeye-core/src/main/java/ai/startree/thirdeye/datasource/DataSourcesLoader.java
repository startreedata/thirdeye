/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.datasource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper classes to load/transform datasources from file
 */
@Singleton
public class DataSourcesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourcesLoader.class);

  private final Map<String, ThirdEyeDataSourceFactory> dataSourceFactoryMap = new HashMap<>();

  @Inject
  public DataSourcesLoader() {
  }

  public void addThirdEyeDataSourceFactory(ThirdEyeDataSourceFactory f) {
    checkState(!dataSourceFactoryMap.containsKey(f.name()),
        "Duplicate ThirdEyeDataSourceFactory: " + f.name());

    dataSourceFactoryMap.put(f.name(), f);
  }

  public ThirdEyeDataSource loadDataSource(DataSourceDTO dataSource) {
    try {
      final String factoryName = dataSource.getType();
      checkArgument(dataSourceFactoryMap.containsKey(factoryName),
          "Data Source type not loaded: " + factoryName);

      LOG.info("Creating thirdeye datasource type {} with properties '{}'",
          factoryName,
          dataSource.getProperties());

      final ThirdEyeDataSource thirdEyeDataSource = dataSourceFactoryMap
          .get(factoryName)
          .build(buildContext(dataSource));
      return requireNonNull(thirdEyeDataSource, "thirdEyeDataSource is null");
    } catch (Exception e) {
      LOG.error(String.format("Exception creating data source. name: %s, type: %s",
              dataSource.getName(),
              dataSource.getType()),
          e);
      throw e;
    }
  }

  private ThirdEyeDataSourceContext buildContext(final DataSourceDTO dataSource) {
    return new ThirdEyeDataSourceContext().setDataSourceDTO(dataSource);
  }
}
