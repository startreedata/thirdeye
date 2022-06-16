/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
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

  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final Map<String, ThirdEyeDataSourceFactory> dataSourceFactoryMap = new HashMap<>();
  private final MetricRegistry metricRegistry;

  @Inject
  public DataSourcesLoader(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricRegistry metricRegistry) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricRegistry = metricRegistry;
  }

  public void addThirdEyeDataSourceFactory(ThirdEyeDataSourceFactory f) {
    checkState(!dataSourceFactoryMap.containsKey(f.name()),
        "Duplicate ThirdEyeDataSourceFactory: " + f.name());

    dataSourceFactoryMap.put(f.name(), f);
  }

  public ThirdEyeDataSource loadDataSource(DataSourceDTO dataSource) {
    final String factoryName = dataSource.getType();
    try {
      checkArgument(dataSourceFactoryMap.containsKey(factoryName),
          "Data Source type not loaded: " + factoryName);

      LOG.info("Creating thirdeye datasource type {} with properties '{}'",
          factoryName,
          dataSource.getProperties());

      final ThirdEyeDataSource ds = dataSourceFactoryMap
          .get(factoryName)
          .build(buildContext(dataSource));
      metricRegistry.register(String.format("%sDatasource_HEALTH", ds.getName()),
          new Gauge<Boolean>() {
            @Override
            public Boolean getValue() {
              return ds.validate();
            }
          });
      return ds;
    } catch (Exception e) {
      LOG.error("Exception in creating thirdeye data source type {}", factoryName, e);
    }
    return null;
  }

  private ThirdEyeDataSourceContext buildContext(final DataSourceDTO dataSource) {
    return new ThirdEyeDataSourceContext()
        .setDataSourceDTO(dataSource)
        .setMetricConfigManager(metricConfigManager)
        .setDatasetConfigManager(datasetConfigManager);
  }
}
