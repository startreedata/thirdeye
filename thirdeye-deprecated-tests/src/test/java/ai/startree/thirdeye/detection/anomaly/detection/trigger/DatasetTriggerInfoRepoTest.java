/*
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.startree.thirdeye.detection.anomaly.detection.trigger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DatasetTriggerInfoRepoTest {

  private static final String TEST_DATA_SOURCE = "TestSource";
  private static final String TEST_DATASET_PREFIX = "test_dataset_";
  private static final String TEST_METRIC_PREFIX = "test_metric_";

  private DatasetTriggerInfoRepo datasetTriggerInfoRepo;
  private TestDbEnv testDAOProvider;
  private AlertManager alertManager;
  private MetricConfigManager metricConfigManager;
  private DatasetConfigManager datasetConfigDAO;

  @BeforeMethod
  public void BeforeMethod() {
    testDAOProvider = new TestDbEnv();
    final Injector injector = testDAOProvider.getInjector();

    alertManager = injector.getInstance(AlertManager.class);
    metricConfigManager = injector.getInstance(MetricConfigManager.class);
    datasetConfigDAO = injector.getInstance(DatasetConfigManager.class);
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
    detect1.setName("test_detection_1");
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
    datasetConfigDAO.save(ds1);

    DatasetConfigDTO ds2 = new DatasetConfigDTO();
    ds2.setDataset(TEST_DATASET_PREFIX + 2);
    ds2.setDataSource(TEST_DATA_SOURCE);
    ds2.setLastRefreshTime(2000);
    datasetConfigDAO.save(ds2);
    datasetTriggerInfoRepo.init(Collections.singletonList(TEST_DATA_SOURCE));
  }

  @Test
  public void testInitAndGetInstance() {
    assertTrue(this.datasetTriggerInfoRepo.isDatasetActive(TEST_DATASET_PREFIX + 1));
    assertTrue(this.datasetTriggerInfoRepo.isDatasetActive(TEST_DATASET_PREFIX + 2));
    assertFalse(this.datasetTriggerInfoRepo.isDatasetActive(TEST_DATASET_PREFIX + 3));
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 1), 1000);
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 2), 2000);
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 3), 0);
  }

  @Test(dependsOnMethods = {"testInitAndGetInstance"})
  public void testSetLastUpdateTimestamp() {
    DatasetTriggerInfoRepo datasetTriggerInfoRepo = this.datasetTriggerInfoRepo;
    datasetTriggerInfoRepo.setLastUpdateTimestamp(TEST_DATASET_PREFIX + 1, 3000);

    assertEquals(datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 1), 3000);
    assertEquals(datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 2), 2000);
    assertEquals(datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 4), 0);
  }

  @Test(dependsOnMethods = {"testSetLastUpdateTimestamp"}, enabled = false)
  public void testRefresh() throws InterruptedException {
    MetricConfigDTO metric = new MetricConfigDTO();
    metric.setDataset(TEST_DATASET_PREFIX + 3);
    metric.setName(TEST_METRIC_PREFIX + 3);
    metric.setActive(true);
    metric.setAlias("");
    long metricId = metricConfigManager.save(metric);

    AlertDTO detect2 = new AlertDTO();
    detect2.setName("test_detection_2");
    detect2.setActive(true);
    Map<String, Object> props = new HashMap<>();
    List<String> metricUrns = new ArrayList<>();
    metricUrns.add("thirdeye:metric:" + metricId);
    props.put("nestedMetricUrns", metricUrns);
    detect2.setProperties(props);
    alertManager.save(detect2);

    DatasetConfigDTO ds = new DatasetConfigDTO();
    ds.setDataset(TEST_DATASET_PREFIX + 3);
    ds.setDataSource(TEST_DATA_SOURCE);
    ds.setLastRefreshTime(3000);
    datasetConfigDAO.save(ds);

    Thread.sleep(65 * 1000); // wait for datasetTriggerInfoRepo to refresh
    assertTrue(this.datasetTriggerInfoRepo.isDatasetActive(TEST_DATASET_PREFIX + 3));
    assertEquals(this.datasetTriggerInfoRepo.getLastUpdateTimestamp(TEST_DATASET_PREFIX + 3), 3000);
  }

  @AfterMethod
  public void afterMethod() {
    datasetTriggerInfoRepo.close();
    testDAOProvider.cleanup();
  }
}
