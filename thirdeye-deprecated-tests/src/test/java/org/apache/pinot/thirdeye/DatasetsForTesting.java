package org.apache.pinot.thirdeye;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.apache.pinot.thirdeye.dataframe.DataFrame;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.datasource.csv.CSVThirdEyeDataSource;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.joda.time.Period;

public class DatasetsForTesting {

  private static final String DATA_SOURCE_NAME = "myDataSource";
  private static final String DATASET_NAME = "myDataset";
  private static final String COL_NAME = "metric_col";
  private static final String METRIC_NAME = "myMetric";

  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;

  @Inject
  public DatasetsForTesting(
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
  }

  public void load() {
    final DatasetConfigDTO dataset = createDataset(DATASET_NAME,
        DATA_SOURCE_NAME,
        DataFrame.COL_TIME,
        Collections.emptyList());
    datasetConfigManager.save(dataset);
    final MetricConfigDTO metric = createMetric();
    metricConfigManager.save(metric);

    final DataSourceCache dataSourceCache = buildDataSourceCache(metric);
    thirdEyeCacheRegistry.registerQueryCache(dataSourceCache);
  }

  private DataFrame buildDataFrame() {
    return new DataFrame()
        .addSeries(DataFrame.COL_TIME, 1526414678000L, 1527019478000L)
        .addSeries(COL_NAME, 100, 200);
  }

  private Map<String, DataFrame> buildDatasetsMap() {
    final DataFrame df = buildDataFrame();
    return ImmutableMap.of(DATASET_NAME, df);
  }

  private DataSourceCache buildDataSourceCache(MetricConfigDTO metric) {
    final Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    dataSourceMap.put(DATA_SOURCE_NAME, CSVThirdEyeDataSource.fromDataFrame(
        buildDatasetsMap(),
        ImmutableMap.of(metric.getId(), metric.getName())));
    return new DataSourceCache(dataSourceMap, Executors.newSingleThreadExecutor());
  }

  private MetricConfigDTO createMetric() {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setName(METRIC_NAME);
    metricConfigDTO.setDataset(DATASET_NAME);
    metricConfigDTO.setAlias("test");
    return metricConfigDTO;
  }

  public DatasetConfigDTO createDataset(String datasetName,
      String dataSourceName,
      String timeColumnName,
      List<String> dimensionNames
  ) {
    // Create DatasetConfig
    final DatasetConfigDTO dataset = new DatasetConfigDTO();
    dataset.setDataset(datasetName);
    dataset.setDimensions(dimensionNames);
    dataset.setDataSource(dataSourceName);
    dataset.setActive(true);
    dataset.setAdditive(true);

    final Period period = Period.days(1);
    dataset.setTimezone("America/Los_Angeles");
    dataset.setTimeDuration(ThirdEyeUtils.getTimeDuration(period));
    dataset.setTimeUnit(ThirdEyeUtils.getTimeUnit(period));
    dataset.setTimeColumn(timeColumnName);
    dataset.setTimeFormat("SIMPLE_DATE_FORMAT");
    return dataset;
  }
}
