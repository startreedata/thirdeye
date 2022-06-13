/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.HashSet;
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
  private static final String derivedMetric1 = "metric3";
  private Long metricConfigId1;
  private Long metricConfigId2;
  private Long derivedMetricConfigId;
  private MetricConfigManager metricConfigDAO;

  @BeforeClass
  void beforeClass() {
    Injector injector = new TestDatabase().createInjector();
    metricConfigDAO = injector.getInstance(MetricConfigManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
  }

  @Test
  public void testCreate() {

    MetricConfigDTO metricConfig1 = DatalayerTestUtils.getTestMetricConfig(dataset1, metric1, null);
    metricConfig1.setActive(false);
    metricConfigId1 = metricConfigDAO.save(metricConfig1);
    Assert.assertNotNull(metricConfigId1);

    metricConfigId2 = metricConfigDAO
        .save(DatalayerTestUtils.getTestMetricConfig(dataset2, metric2, null));
    Assert.assertNotNull(metricConfigId2);

    MetricConfigDTO metricConfig3 = DatalayerTestUtils.getTestMetricConfig(dataset1,
        derivedMetric1,
        null);
    metricConfig3.setDerivedMetricExpression("id" + metricConfigId1 + "/id" + metricConfigId2);
    derivedMetricConfigId = metricConfigDAO.save(metricConfig3);
    Assert.assertNotNull(derivedMetricConfigId);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFind() {
    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findAll();
    Assert.assertEquals(metricConfigs.size(), 3);

    metricConfigs = metricConfigDAO.findByDataset(dataset1);
    Assert.assertEquals(metricConfigs.size(), 2);

    metricConfigs = metricConfigDAO.findActiveByDataset(dataset1);
    Assert.assertEquals(metricConfigs.size(), 1);

    MetricConfigDTO metricConfig = metricConfigDAO.findByMetricAndDataset(metric1, dataset1);
    Assert.assertEquals(metricConfig.getId(), metricConfigId1);
  }

  @Test(dependsOnMethods = {"testFind"})
  public void testFindLike() {
    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findWhereNameOrAliasLikeAndActive("%m%");
    Assert.assertEquals(metricConfigs.size(), 2);
    metricConfigs = metricConfigDAO.findWhereNameOrAliasLikeAndActive("%2%");
    Assert.assertEquals(metricConfigs.size(), 1);
    metricConfigs = metricConfigDAO.findWhereNameOrAliasLikeAndActive("%1%");
    Assert.assertEquals(metricConfigs.size(), 1);
    metricConfigs = metricConfigDAO.findWhereNameOrAliasLikeAndActive("%p%");
    Assert.assertEquals(metricConfigs.size(), 0);
  }

  @Test(dependsOnMethods = {"testFindLike", "testFindByAlias"})
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

  @Test(dependsOnMethods = {"testFind"})
  public void testFindByAlias() {
    List<MetricConfigDTO> metricConfigs = metricConfigDAO.findWhereAliasLikeAndActive(
        new HashSet<>(Arrays.asList("1", "3")));
    Assert.assertEquals(metricConfigs.size(), 1);

    metricConfigs = metricConfigDAO.findWhereAliasLikeAndActive(
        new HashSet<>(Arrays.asList("etric", "m")));
    Assert.assertEquals(metricConfigs.size(), 2);
  }
}
