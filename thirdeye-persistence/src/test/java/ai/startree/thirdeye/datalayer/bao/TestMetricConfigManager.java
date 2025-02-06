/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestMetricConfigManager {

  private static final String dataset1 = "my dataset1";
  private static final String dataset2 = "my dataset2";
  private static final String metric1 = "metric1";
  private static final String metric2 = "metric2";
  private static final String NAMESPACE_1 = "namespace1";
  private static final String derivedMetric1 = "metric3";
  private Long metricConfigId1;
  private Long metricConfigId2;
  private Long derivedMetricConfigId;
  private MetricConfigManager metricConfigDAO;

  @BeforeClass
  void beforeClass() {
    metricConfigDAO = MySqlTestDatabase.sharedInjector().getInstance(MetricConfigManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    if (metricConfigDAO != null) {
      metricConfigDAO.findAll().forEach(metricConfigDAO::delete); 
    }
  }

  @Test
  public void testCreate() {

    MetricConfigDTO metricConfig1 = DatalayerTestUtils.getTestMetricConfig(dataset1, metric1,
        NAMESPACE_1, null);
    metricConfig1.setActive(false);
    metricConfigId1 = metricConfigDAO.save(metricConfig1);
    Assert.assertNotNull(metricConfigId1);

    metricConfigId2 = metricConfigDAO
        .save(DatalayerTestUtils.getTestMetricConfig(dataset2, metric2, NAMESPACE_1, null));
    Assert.assertNotNull(metricConfigId2);

    MetricConfigDTO metricConfig3 = DatalayerTestUtils.getTestMetricConfig(dataset1,
        derivedMetric1,
        NAMESPACE_1,
        null);
    metricConfig3.setDerivedMetricExpression("id" + metricConfigId1 + "/id" + metricConfigId2);
    derivedMetricConfigId = metricConfigDAO.save(metricConfig3);
    Assert.assertNotNull(derivedMetricConfigId);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFind() {
    final List<MetricConfigDTO> metricConfigs = metricConfigDAO.findAll();
    Assert.assertEquals(metricConfigs.size(), 3);

    final MetricConfigDTO metricConfig = metricConfigDAO.findBy(metric1,
        dataset1, NAMESPACE_1);
    Assert.assertEquals(metricConfig.getId(), metricConfigId1);
  }

  @Test()
  public void testUpdate() {
    MetricConfigDTO metricConfig = metricConfigDAO.findById(metricConfigId1);
    Assert.assertNotNull(metricConfig);
    Assert.assertFalse(metricConfig.isInverseMetric());
    metricConfig.setInverseMetric(true);
    metricConfigDAO.update(metricConfig);
    metricConfig = metricConfigDAO.findById(metricConfigId1);
    Assert.assertNotNull(metricConfig);
    Assert.assertTrue(metricConfig.isInverseMetric());
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    metricConfigDAO.deleteById(metricConfigId2);
    MetricConfigDTO metricConfig = metricConfigDAO.findById(metricConfigId2);
    Assert.assertNull(metricConfig);
  }
}
