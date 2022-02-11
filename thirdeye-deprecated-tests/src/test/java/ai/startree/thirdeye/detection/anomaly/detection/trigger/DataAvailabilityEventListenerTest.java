/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.detection.trigger;

import static org.testng.Assert.assertEquals;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.filter.ActiveDatasetFilter;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.filter.DataAvailabilityEventFilter;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.filter.OnTimeFilter;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DatasetTriggerInfoRepo;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataAvailabilityEventListenerTest {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  static String TEST_DATA_SOURCE = "TestSource";
  static String TEST_DATASET_PREFIX = "ds_trigger_listener_";
  static String TEST_METRIC_PREFIX = "metric_trigger_listener_";

  private final MockConsumerDataAvailability consumer = new MockConsumerDataAvailability();
  private DatasetTriggerInfoRepo datasetTriggerInfoRepo;
  private TestDbEnv testDAOProvider;
  private DataAvailabilityEventListener dataAvailabilityEventListener;
  private DatasetConfigManager datasetConfigManager;

  @BeforeMethod
  public void beforeMethod() {
    testDAOProvider = new TestDbEnv();
    final Injector injector = testDAOProvider.getInjector();
    AlertManager alertManager = injector.getInstance(AlertManager.class);
    MetricConfigManager metricConfigManager = injector.getInstance(MetricConfigManager.class);
    datasetConfigManager = injector.getInstance(DatasetConfigManager.class);
    datasetTriggerInfoRepo = injector.getInstance(DatasetTriggerInfoRepo.class);

    MetricConfigDTO metric1 = new MetricConfigDTO();
    metric1.setDataset(TEST_DATASET_PREFIX + 1);
    metric1.setName(TEST_METRIC_PREFIX + 1);
    metric1.setActive(true);
    metric1.setAlias("");
    long metricId1 = metricConfigManager.save(metric1);

    MetricConfigDTO metric2 = new MetricConfigDTO();
    metric2.setDataset(TEST_DATASET_PREFIX + 2);
    metric2.setName(TEST_METRIC_PREFIX + 2);
    metric2.setActive(true);
    metric2.setAlias("");
    long metricId2 = metricConfigManager.save(metric2);

    AlertDTO detect1 = new AlertDTO();
    detect1.setName("detection_trigger_listener1");
    detect1.setActive(true);
    Map<String, Object> props = new HashMap<>();
    List<String> metricUrns = new ArrayList<>();
    metricUrns.add("thirdeye:metric:" + metricId1);
    metricUrns.add("thirdeye:metric:" + metricId2);
    props.put("nestedMetricUrns", metricUrns);
    detect1.setProperties(props);
    alertManager.save(detect1);

    DatasetConfigDTO ds1 = new DatasetConfigDTO();
    ds1.setDataset(TEST_DATASET_PREFIX + 1);
    ds1.setDataSource(TEST_DATA_SOURCE);
    ds1.setLastRefreshTime(1000);
    datasetConfigManager.save(ds1);

    DatasetConfigDTO ds2 = new DatasetConfigDTO();
    ds2.setDataset(TEST_DATASET_PREFIX + 2);
    ds2.setDataSource(TEST_DATA_SOURCE);
    ds2.setLastRefreshTime(2000);
    datasetConfigManager.save(ds2);

    datasetTriggerInfoRepo.init(Collections.singletonList(TEST_DATA_SOURCE));
    List<DataAvailabilityEventFilter> filters = new ArrayList<>();
    filters.add(new OnTimeFilter(datasetTriggerInfoRepo));
    filters.add(new ActiveDatasetFilter(datasetTriggerInfoRepo));
    dataAvailabilityEventListener = new DataAvailabilityEventListener(consumer,
        filters,
        0,
        5_000,
        datasetConfigManager,
        datasetTriggerInfoRepo);
  }

  @Test
  public void testUpdateOneDataset() throws InterruptedException {
    dataAvailabilityEventListener.processOneBatch();
    DatasetConfigDTO dataset1 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 1);
    DatasetConfigDTO dataset2 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 2);

    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 1), 2000);
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 2), 3000);
    assertEquals(dataset1.getLastRefreshTime(), 2000);
    assertEquals(dataset2.getLastRefreshTime(), 3000);
  }

  @Test(dependsOnMethods = {"testUpdateOneDataset"})
  public void testUpdateTwoDataset() throws InterruptedException {
    dataAvailabilityEventListener.processOneBatch();
    DatasetConfigDTO dataset1 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 1);
    DatasetConfigDTO dataset2 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 2);

    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 1), 3000);
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 2), 3000);
    assertEquals(dataset1.getLastRefreshTime(), 3000);
    assertEquals(dataset2.getLastRefreshTime(), 3000);
  }

  @Test(dependsOnMethods = {"testUpdateTwoDataset"})
  public void testNoUpdate() throws InterruptedException {
    dataAvailabilityEventListener.processOneBatch();
    DatasetConfigDTO dataset1 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 1);
    DatasetConfigDTO dataset2 = datasetConfigManager.findByDataset(TEST_DATASET_PREFIX + 2);

    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 1), 1000);
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 2), 2000);
    assertEquals(dataset1.getLastRefreshTime(), 1000);
    assertEquals(dataset2.getLastRefreshTime(), 2000);
  }

  @AfterMethod()
  public void afterMethod() {
    dataAvailabilityEventListener.close();
    testDAOProvider.cleanup();
  }
}
