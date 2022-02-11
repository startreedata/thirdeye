/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.mock;

import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.util.DataFrameUtils;
import ai.startree.thirdeye.util.RequestContainer;
import ai.startree.thirdeye.util.TimeSeriesRequestContainer;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Injector;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MockThirdEyeDataSourceIntegrationTest {

  private TestDbEnv testDAOProvider;

  private Long metricPurchasesId;
  private Long metricRevenueId;
  private Long metricAdImpressionsId;
  private Long metricPageViewsId;
  private ThirdEyeCacheRegistry cacheRegistry;

  private long timestamp;
  private DatasetConfigManager datasetConfigDAO;
  private MetricConfigManager metricConfigDAO;
  private DataSourceCache dataSourceCache;

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database.
   * <br/><b>NOTE:</b> this method injects dependencies from the DAO registry.
   *
   * @param slice metric data slice
   * @param reference unique identifier for request
   * @return RequestContainer
   * @see DataFrameUtils#makeTimeSeriesRequest(MetricSlice slice, String, MetricConfigManager,
   *     DatasetConfigManager)
   */
  private TimeSeriesRequestContainer makeTimeSeriesRequest(MetricSlice slice,
      String reference) throws Exception {
    return DataFrameUtils.makeTimeSeriesRequest(slice,
        reference,
        metricConfigDAO,
        datasetConfigDAO,
        cacheRegistry);
  }

  /**
   * Constructs and wraps a request for a metric with derived expressions. Resolves all
   * required dependencies from the Thirdeye database.
   *
   * @param slice metric data slice
   * @param dimensions dimensions to group by
   * @param limit top k element limit ({@code -1} for default)
   * @param reference unique identifier for request
   * @return RequestContainer
   */
  private RequestContainer makeAggregateRequest(MetricSlice slice, List<String> dimensions,
      int limit, String reference, final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    return DataFrameUtils
        .makeAggregateRequest(slice,
            dimensions,
            limit,
            reference,
            metricConfigDAO,
            datasetConfigDAO,
            thirdEyeCacheRegistry);
  }

  @BeforeClass
  void beforeMethod() throws Exception {
    this.testDAOProvider = new TestDbEnv();
    final Injector injector = testDAOProvider.getInjector();
    datasetConfigDAO = injector.getInstance(DatasetConfigManager.class);
    metricConfigDAO = injector.getInstance(MetricConfigManager.class);

    URL dataSourcesConfig = this.getClass().getResource("data-sources-config.yml");

    // NOTE: MockThirdEyeDataSource expects the first IDs to be reserved for metrics

    // metric business::purchases
    MetricConfigDTO metricPurchases = new MetricConfigDTO();
    metricPurchases.setName("purchases");
    metricPurchases.setDataset("business");
    metricPurchases.setAlias("business::purchases");
    this.metricPurchasesId = metricConfigDAO.save(metricPurchases);
    Assert.assertNotNull(this.metricPurchasesId);

    // metric business::revenue
    MetricConfigDTO metricRevenue = new MetricConfigDTO();
    metricRevenue.setName("revenue");
    metricRevenue.setDataset("business");
    metricRevenue.setAlias("business::revenue");
    this.metricRevenueId = metricConfigDAO.save(metricRevenue);
    Assert.assertNotNull(this.metricRevenueId);

    // metric tracking::adImpressions
    MetricConfigDTO metricAdImpressions = new MetricConfigDTO();
    metricAdImpressions.setName("adImpressions");
    metricAdImpressions.setDataset("tracking");
    metricAdImpressions.setAlias("tracking::adImpressions");
    this.metricAdImpressionsId = metricConfigDAO.save(metricAdImpressions);
    Assert.assertNotNull(this.metricAdImpressionsId);

    // metric tracking::pageViews
    MetricConfigDTO metricPageViews = new MetricConfigDTO();
    metricPageViews.setName("pageViews");
    metricPageViews.setDataset("tracking");
    metricPageViews.setAlias("tracking::pageViews");
    this.metricPageViewsId = metricConfigDAO.save(metricPageViews);
    Assert.assertNotNull(this.metricPageViewsId);

    // dataset business
    DatasetConfigDTO datasetBusiness = new DatasetConfigDTO();
    datasetBusiness.setDataset("business");
    datasetBusiness.setDataSource("MockThirdEyeDataSource");
    datasetBusiness.setTimeDuration(1);
    datasetBusiness.setTimeUnit(TimeUnit.DAYS);
    datasetBusiness.setTimezone("America/Los_Angeles");
    datasetBusiness.setDimensions(Arrays.asList("browser", "country"));
    datasetConfigDAO.save(datasetBusiness);
    Assert.assertNotNull(datasetBusiness.getId());

    // dataset tracking
    DatasetConfigDTO datasetTracking = new DatasetConfigDTO();
    datasetTracking.setDataset("tracking");
    datasetTracking.setDataSource("MockThirdEyeDataSource");
    datasetTracking.setTimeDuration(1);
    datasetTracking.setTimeUnit(TimeUnit.HOURS);
    datasetTracking.setTimezone("America/Los_Angeles");
    datasetTracking.setDimensions(Arrays.asList("browser", "country", "platform"));
    datasetConfigDAO.save(datasetTracking);
    Assert.assertNotNull(datasetTracking.getId());

    // data sources and caches
    this.timestamp = System.currentTimeMillis();
    final DataSourcesLoader dataSourcesLoader = new DataSourcesLoader(metricConfigDAO,
        datasetConfigDAO);
    dataSourceCache = new DataSourceCache(
        mock(DataSourceManager.class),
        dataSourcesLoader,
        new MetricRegistry());
    cacheRegistry = new ThirdEyeCacheRegistry(
        metricConfigDAO,
        datasetConfigDAO,
        dataSourceCache);

    cacheRegistry.initializeCaches();
  }

  @AfterClass(alwaysRun = true)
  void afterMethod() {
    this.testDAOProvider.cleanup();
  }

  @Test(enabled = false)
  public void testAggregation() throws Exception {
    MetricSlice slice = MetricSlice
        .from(this.metricPageViewsId, this.timestamp - 7200000, this.timestamp);
    RequestContainer requestContainer = makeAggregateRequest(slice,
        Collections.emptyList(),
        -1,
        "ref",
        cacheRegistry);
    ThirdEyeResponse response = dataSourceCache
        .getQueryResult(requestContainer.getRequest());
    DataFrame df = DataFrameUtils.evaluateResponse(response, requestContainer,
        cacheRegistry);

    Assert.assertTrue(df.getDouble(DataFrame.COL_VALUE, 0) > 0);
  }

  @Test(enabled = false)
  public void testBreakdown() throws Exception {
    MetricSlice slice = MetricSlice
        .from(this.metricRevenueId, this.timestamp - TimeUnit.HOURS.toMillis(25),
            this.timestamp); // allow for DST
    RequestContainer requestContainer = makeAggregateRequest(slice,
        Arrays.asList("country", "browser"),
        -1,
        "ref",
        cacheRegistry);
    ThirdEyeResponse response = dataSourceCache
        .getQueryResult(requestContainer.getRequest());
    DataFrame df = DataFrameUtils.evaluateResponse(response, requestContainer,
        cacheRegistry);

    Assert.assertEquals(df.size(), 9);
    Assert.assertEquals(new HashSet<>(df.getStrings("country").toList()),
        new HashSet<>(Arrays.asList("ca", "mx", "us")));
    Assert.assertEquals(new HashSet<>(df.getStrings("browser").toList()),
        new HashSet<>(Arrays.asList("chrome", "edge", "safari")));
    for (int i = 0; i < df.size(); i++) {
      Assert.assertTrue(df.getDouble(DataFrame.COL_VALUE, i) >= 0);
    }
  }

  @Test(enabled = false)
  public void testTimeSeries() throws Exception {
    MetricSlice slice = MetricSlice
        .from(this.metricPageViewsId, this.timestamp - 7200000, this.timestamp);
    TimeSeriesRequestContainer requestContainer = makeTimeSeriesRequest(slice, "ref");
    ThirdEyeResponse response = dataSourceCache.getQueryResult(requestContainer.getRequest());
    DataFrame df = DataFrameUtils.evaluateResponse(response, requestContainer,
        cacheRegistry);

    Assert.assertEquals(df.size(), 2);
    Assert.assertTrue(df.getLong(DataFrame.COL_TIME, 0) > 0);
    Assert.assertTrue(df.getDouble(DataFrame.COL_VALUE, 0) > 0);
    Assert.assertTrue(df.getLong(DataFrame.COL_TIME, 1) > 0);
    Assert.assertTrue(df.getDouble(DataFrame.COL_VALUE, 1) > 0);
  }

  @Test(enabled = false)
  public void testAggregationWithFilter() throws Exception {
    SetMultimap<String, String> filtersBasic = HashMultimap.create();
    filtersBasic.put("browser", "safari");
    filtersBasic.put("browser", "firefox");

    SetMultimap<String, String> filtersMobile = HashMultimap.create(filtersBasic);
    filtersMobile.put("platform", "mobile");

    SetMultimap<String, String> filtersDesktop = HashMultimap.create(filtersBasic);
    filtersDesktop.put("platform", "desktop");

    MetricSlice sliceBasic = MetricSlice
        .from(this.metricAdImpressionsId, this.timestamp - 7200000, this.timestamp, filtersBasic);
    MetricSlice sliceMobile = MetricSlice
        .from(this.metricAdImpressionsId, this.timestamp - 7200000, this.timestamp, filtersMobile);
    MetricSlice sliceDesktop = MetricSlice
        .from(this.metricAdImpressionsId, this.timestamp - 7200000, this.timestamp, filtersDesktop);

    RequestContainer reqBasic = makeAggregateRequest(sliceBasic, Collections.emptyList(), -1, "ref",
        cacheRegistry);
    ThirdEyeResponse resBasic = dataSourceCache.getQueryResult(reqBasic.getRequest());
    DataFrame dfBasic = DataFrameUtils.evaluateResponse(resBasic, reqBasic,
        cacheRegistry);

    RequestContainer reqMobile = makeAggregateRequest(sliceMobile,
        Collections.emptyList(),
        -1,
        "ref",
        cacheRegistry);
    ThirdEyeResponse resMobile = dataSourceCache.getQueryResult(reqMobile.getRequest());
    DataFrame dfMobile = DataFrameUtils.evaluateResponse(resMobile, reqMobile,
        cacheRegistry);

    RequestContainer reqDesktop = makeAggregateRequest(sliceDesktop,
        Collections.emptyList(),
        -1,
        "ref",
        cacheRegistry);
    ThirdEyeResponse resDesktop = dataSourceCache.getQueryResult(reqDesktop.getRequest());
    DataFrame dfDesktop = DataFrameUtils.evaluateResponse(resDesktop, reqDesktop,
        cacheRegistry);

    Assert.assertTrue(
        dfBasic.getDouble(DataFrame.COL_VALUE, 0) >= dfMobile.getDouble(DataFrame.COL_VALUE, 0));
    Assert.assertTrue(
        dfBasic.getDouble(DataFrame.COL_VALUE, 0) >= dfDesktop.getDouble(DataFrame.COL_VALUE, 0));
    Assert.assertEquals(dfBasic.getDouble(DataFrame.COL_VALUE, 0),
        dfDesktop.getDouble(DataFrame.COL_VALUE, 0) + dfMobile.getDouble(DataFrame.COL_VALUE, 0));
  }
}
