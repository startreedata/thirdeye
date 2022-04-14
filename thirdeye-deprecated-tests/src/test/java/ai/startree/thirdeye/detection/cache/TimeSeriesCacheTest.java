/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TimeSeriesCacheTest {

  private static final String COLLECTION = "collection";
  private static final MetricDataset METRIC = new MetricDataset("metric", COLLECTION);
  private static final MetricFunction metricFunction = new MetricFunction(MetricAggFunction.AVG,
      METRIC.getMetricName(),
      1L,
      COLLECTION,
      null,
      null);

  private static final ThirdEyeRequest request = ThirdEyeRequest.newBuilder()
      .setMetricFunctions(Collections.singletonList(metricFunction))
      .setStartTimeInclusive(new DateTime(0, DateTimeZone.UTC))
      .setEndTimeExclusive(new DateTime(10000, DateTimeZone.UTC))
      .setGroupByTimeGranularity(TimeGranularity.fromString("1_SECONDS"))
      .setLimit(12345)
      .build("ref");

  private static final TimeSpec
      timeSpec = new TimeSpec(METRIC.getMetricName(),
      TimeGranularity.fromString("1_SECONDS"),
      TimeSpec.SINCE_EPOCH_FORMAT);

  private static final String metricUrn = MetricEntity.fromMetric(
      request.getFilterSet().asMap(), metricFunction.getMetricId()).getUrn();

  private final CacheConfig config = CacheConfig.getInstance();
  private final List<TimeSeriesDataPoint> pretendCacheStore = new ArrayList<>();
  private ExecutorService executor;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private DataSourceCache dataSourceCache;
  private CouchbaseCacheDAO cacheDAO;
  private DefaultTimeSeriesCache cache;

  @BeforeMethod
  public void beforeMethod() throws Exception {

    // mock QueryCache so it doesn't call data source
    dataSourceCache = mock(DataSourceCache.class);

    // mock DAO object so that inserts put data into list
    cacheDAO = mock(CouchbaseCacheDAO.class);
    doAnswer(invocation -> pretendCacheStore.add((TimeSeriesDataPoint) invocation.getArguments()[0]))
        .when(cacheDAO)
        .insertTimeSeriesDataPoint(any(TimeSeriesDataPoint.class));

    datasetDAO = mock(DatasetConfigManager.class);
    metricDAO = mock(MetricConfigManager.class);

    executor = Executors.newSingleThreadExecutor();

    cache = new DefaultTimeSeriesCache(datasetDAO,
        cacheDAO,
        config,
        dataSourceCache);
  }

  @AfterMethod
  public void afterMethod() {
    config.setUseCentralizedCache(config.useCentralizedCache());
    pretendCacheStore.clear();
    reset(cacheDAO, datasetDAO, metricDAO);
  }

  @Test(enabled = false)
  public void testInsertTimeSeriesIntoCache() throws InterruptedException {
    List<String[]> rows = buildRawResponseRowsWithTimestamps(0, 10);

    ThirdEyeResponse response = new RelationalThirdEyeResponse(request, rows, timeSpec);
    cache.insertTimeSeriesIntoCache(response);

    executor.shutdown();
    final boolean result = executor.awaitTermination(10, TimeUnit.SECONDS);
    assertThat(result).isTrue();

    verify(cacheDAO, times(10)).insertTimeSeriesDataPoint(any(TimeSeriesDataPoint.class));

    Assert.assertEquals(pretendCacheStore.size(), 10);
    verifyTimeSeriesListCorrectness();
  }

  @Test
  public void testTryFetchExistingTimeSeriesWithoutCentralizedCacheEnabled() throws Exception {
    config.setUseCentralizedCache(false);

    List<String[]> rows = buildRawResponseRowsWithTimestamps(0, 10);

    // mock queryCache to return full rows
    when(dataSourceCache.getQueryResult(any(ThirdEyeRequest.class)))
        .thenAnswer(invocation -> new RelationalThirdEyeResponse(
            (ThirdEyeRequest) invocation.getArguments()[0],
            rows,
            timeSpec));

    ThirdEyeResponse response = dataSourceCache.getQueryResult(request);
    verifyRowSeriesCorrectness(response);
  }

  @Test(enabled = false) // TODO check and fix flaky test
  public void testTryFetchExistingTimeSeriesFromCentralizedCacheWithEmptyCache() throws Exception {

    config.setUseCentralizedCache(true);

    // mock tryFetch method to return a ThirdEyeResponse with nothing in it.
    when(cacheDAO.tryFetchExistingTimeSeries(any(ThirdEyeCacheRequest.class)))
        .thenAnswer(invocation -> new ThirdEyeCacheResponse(
            (ThirdEyeCacheRequest) invocation.getArguments()[0], new ArrayList<>()));

    when(datasetDAO.findByDataset(anyString()))
        .thenReturn(makeDatasetDTO());

    List<String[]> rows = buildRawResponseRowsWithTimestamps(0, 10);

    // mock queryCache to return full rows
    when(dataSourceCache.getQueryResult(any(ThirdEyeRequest.class)))
        .thenAnswer(invocation -> {
          ThirdEyeRequest request = (ThirdEyeRequest) invocation.getArguments()[0];
          Assert.assertEquals(request.getStartTimeInclusive().getMillis(), 0);
          Assert.assertEquals(request.getEndTimeExclusive().getMillis(), 10000);
          return new RelationalThirdEyeResponse(request, rows, timeSpec);
        });

    ThirdEyeResponse response = cache.fetchTimeSeries(request);

    // verify that the missing data points were inserted into the cache after miss.
    executor.shutdown();
    final boolean result = executor.awaitTermination(15, TimeUnit.SECONDS);
    assertThat(result).isTrue();

    verify(cacheDAO, times(10)).insertTimeSeriesDataPoint(any(TimeSeriesDataPoint.class));
    Assert.assertEquals(pretendCacheStore.size(), 10);

    verifyTimeSeriesListCorrectness();
    verifyRowSeriesCorrectness(response);
    verifyTimeSpec(response.getDataTimeSpec());
  }

  @Test
  public void testTryFetchExistingTimeSeriesFromCentralizedCacheWithFullCache() throws Exception {

    config.setUseCentralizedCache(true);

    List<TimeSeriesDataPoint> dataPoints = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      TimeSeriesDataPoint dp = new TimeSeriesDataPoint(metricUrn, i * 1000, 1, String.valueOf(i));
      dataPoints.add(dp);
    }

    // mock tryFetch method to return a ThirdEyeResponse with the correct data points in it.
    when(cacheDAO.tryFetchExistingTimeSeries(any(ThirdEyeCacheRequest.class)))
        .thenAnswer(invocation -> new ThirdEyeCacheResponse(
            (ThirdEyeCacheRequest) invocation.getArguments()[0], dataPoints));

    when(datasetDAO.findByDataset(anyString())).thenReturn(makeDatasetDTO());

    ThirdEyeResponse response = cache.fetchTimeSeries(request);
    verifyRowSeriesCorrectness(response);
  }

  /**
   * TODO: figure out how to test cases where the cache only has partial data.
   * Right now it's difficult to do so because we are using DataFrameUtils.makeTimeSeriesRequestAligned(),
   * which is a static method. We may have to refactor our code to do these tests. One idea could
   * be
   * to
   * have all classes that use TimeSeriesLoader use TimeSeriesCache instead, and TimeSeriesCache is
   * the
   * only class to use TimeSeriesLoader.
   */

//  @Test
//  public void testTryFetchExistingTimeSeriesFromCentralizedCacheWithMissingStartSlice() throws Exception {
//    config.setUseCentralizedCache(true);
//
//    List<TimeSeriesDataPoint> dataPoints = new ArrayList<>();
//    for (int i = 0; i < 5; i++) {
//      TimeSeriesDataPoint dp = new TimeSeriesDataPoint(metricUrn, i * 1000, 1, String.valueOf(i));
//      dataPoints.add(dp);
//    }
//
//    Mockito
//        .when(cacheDAO.tryFetchExistingTimeSeries(any(ThirdEyeCacheRequest.class)))
//        .thenAnswer(invocation -> {
//          return new ThirdEyeCacheResponse(
//              (ThirdEyeCacheRequest)invocation.getArguments()[0], dataPoints);
//        });
//
//    Mockito.when(datasetDAO.findByDataset(anyString())).thenReturn(makeDatasetDTO());
//
//    List<String[]> rows = buildRawResponseRowsWithTimestamps(5, 10);
//
//    // mock queryCache to return missing rows
//    Mockito
//        .when(queryCache.getQueryResult(any(ThirdEyeRequest.class)))
//        .thenAnswer(invocation -> {
//          return new RelationalThirdEyeResponse(
//              (ThirdEyeRequest)invocation.getArguments()[0],
//              rows,
//              timeSpec);
//        });
//
//    ThirdEyeResponse response = cache.fetchTimeSeries(request);
//    verifyRowSeriesCorrectness(response);
//  }
  private List<String[]> buildRawResponseRowsWithTimestamps(int start, int end) {
    List<String[]> rows = new ArrayList<>();
    // index 0 is time bucket id, index 1 is value, index 2 is timestamp
    for (int i = start; i < end; i++) {
      String[] rawTimeSeriesDataPoint = new String[3];
      rawTimeSeriesDataPoint[0] = String.valueOf(i);
      rawTimeSeriesDataPoint[1] = String.valueOf(i);
      rawTimeSeriesDataPoint[2] = String.valueOf(i * 1000);
      rows.add(rawTimeSeriesDataPoint);
    }

    return rows;
  }

  private void verifyTimeSeriesListCorrectness() {
    pretendCacheStore.forEach(dp -> {
      assertThat(dp.getMetricId()).isEqualTo(metricFunction.getMetricId().longValue());
      assertThat(dp.getMetricUrn()).isEqualTo(metricUrn);
    });
    Set<Long> expectedTimestamps = new HashSet<>();
    Set<String> expectedDataValues = new HashSet<>();
    for (int i = 0; i < 10; i++) {
      expectedTimestamps.add(i * 1000L);
      expectedDataValues.add(String.valueOf(i));
    }
    assertThat(pretendCacheStore.stream()
        .map(TimeSeriesDataPoint::getTimestamp)
        .collect(Collectors.toSet()))
        .isEqualTo(expectedTimestamps);

    assertThat(pretendCacheStore.stream()
        .map(TimeSeriesDataPoint::getDataValue)
        .collect(Collectors.toSet()))
        .isEqualTo(expectedDataValues);
  }

  private void verifyRowSeriesCorrectness(ThirdEyeResponse response) {
    for (MetricFunction metric : response.getMetricFunctions()) {
      for (int i = 0; i < response.getNumRowsFor(metric); i++) {
        Map<String, String> row = response.getRow(metric, i);
        Assert.assertEquals(row.get("AVG_metric"), String.valueOf(i));
      }
    }
  }

  private void verifyTimeSpec(TimeSpec spec) {
    //Assert.assertEquals(timeSpec.getColumnName(), spec.getColumnName());
    Assert.assertEquals(timeSpec.getDataGranularity(), spec.getDataGranularity());
    Assert.assertEquals(timeSpec.getFormat(), spec.getFormat());
  }

  private DatasetConfigDTO makeDatasetDTO() {
    DatasetConfigDTO datasetDTO = new DatasetConfigDTO();
    datasetDTO.setTimeFormat(TimeSpec.SINCE_EPOCH_FORMAT);
    datasetDTO.setTimeDuration(1);
    datasetDTO.setTimeUnit(TimeUnit.SECONDS);
    datasetDTO.setTimezone("UTC");
    datasetDTO.setTimeColumn(METRIC.getMetricName());

    return datasetDTO;
  }
}
