/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.DAORegistry;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.datasource.csv.CSVThirdEyeDataSource;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.detection.cache.builder.AnomaliesCacheBuilder;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.spi.detection.model.AnomalySlice;
import ai.startree.thirdeye.spi.detection.model.EventSlice;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataProviderTest {

  private TestDbEnv testBase;

  private DataProvider provider;

  private List<Long> eventIds;
  private List<Long> anomalyIds;
  private List<Long> metricIds;
  private List<Long> datasetIds;
  private List<Long> detectionIds;

  private static MergedAnomalyResultDTO makeAnomaly(Long id, Long configId, long start, long end) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setDetectionConfigId(configId);
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
    anomaly.setId(id);
    anomaly.setChildIds(new HashSet<>());
    anomaly.setType(AnomalyType.DEVIATION);

    return anomaly;
  }

  private static EventDTO makeEvent(long start, long end) {
    return makeEvent(null, start, end, Collections.emptyList());
  }

  //
  // metric
  //

  private static EventDTO makeEvent(long start, long end, Iterable<String> filterStrings) {
    return makeEvent(null, start, end, filterStrings);
  }

  private static EventDTO makeEvent(Long id, long start, long end, Iterable<String> filterStrings) {
    EventDTO event = new EventDTO();
    event.setId(id);
    event.setName(String.format("event-%d-%d", start, end));
    event.setStartTime(start);
    event.setEndTime(end);

    Map<String, List<String>> filters = new HashMap<>();
    for (String fs : filterStrings) {
      String[] parts = fs.split("=");
      if (!filters.containsKey(parts[0])) {
        filters.put(parts[0], new ArrayList<>());
      }
      filters.get(parts[0]).add(parts[1]);
    }

    event.setTargetDimensionMap(filters);

    return event;
  }

  private static EventSlice makeEventSlice(long start, long end, Iterable<String> filterStrings) {
    SetMultimap<String, String> filters = HashMultimap.create();
    for (String fs : filterStrings) {
      String[] parts = fs.split("=");
      filters.put(parts[0], parts[1]);
    }
    return new EventSlice(start, end, filters);
  }

  private static AnomalySlice makeAnomalySlice(long start, long end,
      Iterable<String> filterStrings) {
    SetMultimap<String, String> filters = HashMultimap.create();
    for (String fs : filterStrings) {
      String[] parts = fs.split("=");
      filters.put(parts[0], parts[1]);
    }
    return new AnomalySlice().withStart(start).withEnd(end).withFilters(filters);
  }

  //
  // datasets
  //

  private static MetricConfigDTO makeMetric(Long id, String metric, String dataset) {
    MetricConfigDTO metricDTO = new MetricConfigDTO();
    metricDTO.setId(id);
    metricDTO.setName(metric);
    metricDTO.setDataset(dataset);
    metricDTO.setDefaultAggFunction(MetricAggFunction.SUM);
    metricDTO.setAlias(dataset + "::" + metric);
    return metricDTO;
  }

  private static DatasetConfigDTO makeDataset(Long id, String dataset) {
    DatasetConfigDTO datasetDTO = new DatasetConfigDTO();
    datasetDTO.setId(id);
    datasetDTO.setDataSource("myDataSource");
    datasetDTO.setDataset(dataset);
    datasetDTO.setTimeDuration(3600000);
    datasetDTO.setTimeUnit(TimeUnit.MILLISECONDS);
    return datasetDTO;
  }

  @BeforeMethod
  public void beforeMethod() throws Exception {
    this.testBase = new TestDbEnv();

    DAORegistry reg = TestDbEnv.getInstance();
    final EventManager eventDAO = reg.getEventDAO();
    final MergedAnomalyResultManager anomalyDAO = reg.getMergedAnomalyResultDAO();
    final MetricConfigManager metricDAO = reg.getMetricConfigDAO();
    final DatasetConfigManager datasetDAO = reg.getDatasetConfigDAO();
    final EvaluationManager evaluationDAO = reg.getEvaluationManager();
    final AlertManager detectionDAO = reg.getDetectionConfigManager();
    // events
    this.eventIds = new ArrayList<>();
    this.eventIds.add(eventDAO.save(makeEvent(3600000L, 7200000L)));
    this.eventIds.add(eventDAO.save(makeEvent(10800000L, 14400000L)));
    this.eventIds.add(
        eventDAO.save(makeEvent(14400000L, 18000000L, Arrays.asList("a=1", "b=4", "b=2"))));
    this.eventIds
        .add(eventDAO.save(makeEvent(604800000L, 1209600000L, Arrays.asList("b=2", "c=3"))));
    this.eventIds
        .add(eventDAO.save(makeEvent(1209800000L, 1210600000L, Collections.singleton("b=4"))));

    // detections
    this.detectionIds = new ArrayList<>();
    AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName("test_detection_1");
    detectionConfig.setDescription("test_description_1");
    this.detectionIds.add(detectionDAO.save(detectionConfig));
    detectionConfig.setName("test_detection_2");
    detectionConfig.setDescription("test_description_2");
    this.detectionIds.add(detectionDAO.save(detectionConfig));

    // anomalies
    this.anomalyIds = new ArrayList<>();
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(0), 4000000L, 8000000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(0), 8000000L, 12000000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(1), 604800000L, 1209600000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(1), 14400000L, 18000000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(1), 14400000L, 18000000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(1), 14400000L, 18000000L)));
    this.anomalyIds.add(anomalyDAO.save(
        makeAnomaly(null, detectionIds.get(1), 14400000L, 18000000L)));

    // metrics
    this.metricIds = new ArrayList<>();
    this.metricIds.add(metricDAO.save(makeMetric(null, "myMetric1", "myDataset1")));
    this.metricIds.add(metricDAO.save(makeMetric(null, "myMetric2", "myDataset2")));
    this.metricIds.add(metricDAO.save(makeMetric(null, "myMetric3", "myDataset1")));

    // datasets
    this.datasetIds = new ArrayList<>();
    this.datasetIds.add(datasetDAO.save(makeDataset(null, "myDataset1")));
    this.datasetIds.add(datasetDAO.save(makeDataset(null, "myDataset2")));

    // data
    DataFrame data;
    try (Reader dataReader = new InputStreamReader(
        this.getClass().getResourceAsStream("/csv/timeseries-4w.csv"))) {
      data = DataFrame.fromCsv(dataReader);
      data.setIndex(DataFrame.COL_TIME);
      data
          .addSeries(DataFrame.COL_TIME, data.getLongs(DataFrame.COL_TIME).multiply(1000));
    }

    // register caches

    LoadingCache<String, DatasetConfigDTO> mockDatasetConfigCache = mock(LoadingCache.class);
    DatasetConfigDTO datasetConfig = datasetDAO.findByDataset("myDataset2");
    when(mockDatasetConfigCache.get("myDataset2")).thenReturn(datasetConfig);

    LoadingCache<String, Long> mockDatasetMaxDataTimeCache = mock(LoadingCache.class);
    when(mockDatasetMaxDataTimeCache.get("myDataset2"))
        .thenReturn(Long.MAX_VALUE);

    MetricDataset metricDataset = new MetricDataset("myMetric2", "myDataset2");
    LoadingCache<MetricDataset, MetricConfigDTO> mockMetricConfigCache = mock(LoadingCache.class);
    MetricConfigDTO metricConfig = metricDAO.findByMetricAndDataset("myMetric2", "myDataset2");
    when(mockMetricConfigCache.get(metricDataset)).thenReturn(metricConfig);

    Map<String, DataFrame> datasets = new HashMap<>();
    datasets.put("myDataset1", data);
    datasets.put("myDataset2", data);

    Map<Long, String> id2name = new HashMap<>();
    id2name.put(this.metricIds.get(1), "value");
    final CSVThirdEyeDataSource csvThirdEyeDataSource = CSVThirdEyeDataSource.fromDataFrame(datasets,
        id2name);

    final DataSourceManager dataSourceManager = mock(DataSourceManager.class);
    final DataSourceDTO csvDataSource = new DataSourceDTO();
    when(dataSourceManager.findByPredicate(Predicate.EQ("name", "myDataSource")))
        .thenReturn(singletonList(csvDataSource));

    final DataSourcesLoader dataSourcesLoader = mock(DataSourcesLoader.class);
    when(dataSourcesLoader.loadDataSource(csvDataSource)).thenReturn(csvThirdEyeDataSource);
    final DataSourceCache dataSourceCache = new DataSourceCache(dataSourceManager,
        dataSourcesLoader,
        new MetricRegistry());
    final ThirdEyeCacheRegistry cacheRegistry = new ThirdEyeCacheRegistry(
        metricDAO,
        datasetDAO,
        dataSourceCache);

    cacheRegistry.registerMetricConfigCache(mockMetricConfigCache);
    cacheRegistry.registerDatasetConfigCache(mockDatasetConfigCache);
    cacheRegistry.registerDatasetMaxDataTimeCache(mockDatasetMaxDataTimeCache);

    this.provider = new DefaultDataProvider(metricDAO,
        datasetDAO,
        eventDAO,
        evaluationDAO,
        new AnomaliesCacheBuilder(anomalyDAO, CacheConfig.getInstance()));
  }

  //
  // events
  //

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    this.testBase.cleanup();
  }

  @Test
  public void testMetricInvalid() {
    Assert.assertTrue(this.provider.fetchMetrics(Collections.singleton(-1L)).isEmpty());
  }

  @Test
  public void testMetricSingle() {
    MetricConfigDTO metric = this.provider
        .fetchMetrics(Collections.singleton(this.metricIds.get(1))).get(this.metricIds.get(1));

    Assert.assertNotNull(metric);
    Assert.assertEquals(metric, makeMetric(this.metricIds.get(1), "myMetric2", "myDataset2"));
  }

  //
  // anomalies
  //

  @Test
  public void testMetricMultiple() {
    Collection<MetricConfigDTO> metrics = this.provider
        .fetchMetrics(Arrays.asList(this.metricIds.get(1), this.metricIds.get(2))).values();

    Assert.assertEquals(metrics.size(), 2);
    Assert
        .assertTrue(metrics.contains(makeMetric(this.metricIds.get(1), "myMetric2", "myDataset2")));
    Assert
        .assertTrue(metrics.contains(makeMetric(this.metricIds.get(2), "myMetric3", "myDataset1")));
  }

  @Test
  public void testDatasetInvalid() {
    Assert.assertTrue(this.provider.fetchDatasets(Collections.singleton("invalid")).isEmpty());
  }

  @Test
  public void testDatasetSingle() {
    DatasetConfigDTO dataset = this.provider.fetchDatasets(Collections.singleton("myDataset1"))
        .get("myDataset1");

    Assert.assertNotNull(dataset);
    Assert.assertEquals(dataset, makeDataset(this.datasetIds.get(0), "myDataset1"));
  }

  //
  // utils
  //

  @Test
  public void testDatasetMultiple() {
    Collection<DatasetConfigDTO> datasets = this.provider
        .fetchDatasets(Arrays.asList("myDataset1", "myDataset2")).values();

    Assert.assertEquals(datasets.size(), 2);
    Assert.assertTrue(datasets.contains(makeDataset(this.datasetIds.get(0), "myDataset1")));
    Assert.assertTrue(datasets.contains(makeDataset(this.datasetIds.get(1), "myDataset2")));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEventInvalid() {
    this.provider.fetchEvents(Collections.singleton(new EventSlice()));
  }

  @Test
  public void testEventSingle() {
    EventSlice slice = makeEventSlice(4000000L, 4100000L, Collections.emptyList());

    Collection<EventDTO> events = this.provider.fetchEvents(Collections.singleton(slice))
        .get(slice);

    Assert.assertEquals(events.size(), 1);
    Assert.assertTrue(events.contains(
        makeEvent(this.eventIds.get(0), 3600000L, 7200000L, Collections.emptyList())));
  }

  @Test
  public void testEventDimension() {
    EventSlice slice = makeEventSlice(10000000L, 1200000000L, Collections.singleton("b=2"));

    Collection<EventDTO> events = this.provider.fetchEvents(Collections.singleton(slice))
        .get(slice);

    Assert.assertEquals(events.size(), 3);
    Assert.assertTrue(events.contains(
        makeEvent(this.eventIds.get(1), 10800000L, 14400000L, Collections.emptyList())));
    Assert.assertTrue(events.contains(
        makeEvent(this.eventIds.get(2), 14400000L, 18000000L, Arrays.asList("a=1", "b=4", "b=2"))));
    Assert.assertTrue(events.contains(
        makeEvent(this.eventIds.get(3), 604800000L, 1209600000L, Arrays.asList("b=2", "c=3"))));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testAnomalyInvalid() {
    this.provider.fetchAnomalies(Collections.singleton(new AnomalySlice()));
  }

  @Test(enabled = false)
  public void testAnomalySingle() {
    AnomalySlice slice = makeAnomalySlice(1209000000L, -1, Collections.emptyList());

    Collection<MergedAnomalyResultDTO> anomalies = this.provider
        .fetchAnomalies(Collections.singleton(slice)).get(slice);

    Assert.assertEquals(anomalies.size(), 1);
    Assert.assertTrue(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(2), detectionIds.get(1), 604800000L, 1209600000L)));
  }

  @Test(enabled = false)
  public void testAnomalyDimension() {
    AnomalySlice slice = makeAnomalySlice(0, -1, Arrays.asList("a=1", "c=3"));

    Collection<MergedAnomalyResultDTO> anomalies = this.provider
        .fetchAnomalies(Collections.singleton(slice)).get(slice);

    Assert.assertEquals(anomalies.size(), 2);
    Assert.assertTrue(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(0), detectionIds.get(0), 4000000L, 8000000L)));
    Assert.assertTrue(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(3), detectionIds.get(1), 14400000L, 18000000L)));

    Assert.assertFalse(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(2), detectionIds.get(1), 604800000L, 1209600000L)));
  }

  @Test(enabled = false)
  public void testAnomalyMultiDimensions() {
    AnomalySlice slice = makeAnomalySlice(0, -1, Arrays.asList("a=1", "a=2", "c=3"));

    Collection<MergedAnomalyResultDTO> anomalies = this.provider
        .fetchAnomalies(Collections.singleton(slice)).get(slice);
    Assert.assertEquals(anomalies.size(), 2);
    Assert.assertTrue(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(4), detectionIds.get(1), 14400000L, 18000000L)));
    Assert.assertTrue(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(6), detectionIds.get(1), 14400000L, 18000000L)));
    Assert.assertFalse(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(3), detectionIds.get(1), 14400000L, 18000000L)));
    Assert.assertFalse(anomalies.contains(
        makeAnomaly(this.anomalyIds.get(5), detectionIds.get(1), 14400000L, 18000000L)));
  }
}
