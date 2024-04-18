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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestDatasetConfigManager {

  private static final String DATASET_1 = "my dataset1";
  private static final String DATASET_2 = "my dataset2";
  private static final String NAMESPACE_1 = "namespace1";

  private Long datasetConfigId1;
  private Long datasetConfigId2;
  private DatasetConfigManager datasetConfigDAO;

  @BeforeClass
  void beforeClass() {
    datasetConfigDAO = MySqlTestDatabase.sharedInjector().getInstance(DatasetConfigManager.class);
  }

  @AfterClass(alwaysRun = true)
  void clean() {
    datasetConfigDAO.findAll().forEach(datasetConfigDAO::delete);
  }

  @Test
  public void testCreate() {
    DatasetConfigDTO datasetConfig1 = DatalayerTestUtils.getTestDatasetConfig(DATASET_1);
    datasetConfig1.setAuth(new AuthorizationConfigurationDTO().setNamespace(NAMESPACE_1));
    datasetConfigId1 = datasetConfigDAO.save(datasetConfig1);
    Assert.assertNotNull(datasetConfigId1);

    DatasetConfigDTO datasetConfig2 = DatalayerTestUtils.getTestDatasetConfig(DATASET_2);
    // this dataset has no namespace
    datasetConfig2.setActive(false);
    datasetConfigId2 = datasetConfigDAO.save(datasetConfig2);
    Assert.assertNotNull(datasetConfigId2);

    List<DatasetConfigDTO> datasetConfigs = datasetConfigDAO.findAll();
    Assert.assertEquals(datasetConfigs.size(), 2);

    datasetConfigs = datasetConfigDAO.findActive();
    Assert.assertEquals(datasetConfigs.size(), 1);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindByDataset() {
    final DatasetConfigDTO dataset1InNamespace1 = datasetConfigDAO.findByDatasetAndNamespaceOrUnsetNamespace(DATASET_1, NAMESPACE_1);
    assertThat(dataset1InNamespace1.getDataset()).isEqualTo(DATASET_1);
    final DatasetConfigDTO dataset1InUnsetNamespace = datasetConfigDAO.findByDatasetAndNamespaceOrUnsetNamespace(DATASET_1, null);
    assertThat(dataset1InUnsetNamespace).isNull();

    final DatasetConfigDTO dataset2InNamespace1 = datasetConfigDAO.findByDatasetAndNamespaceOrUnsetNamespace(DATASET_2, NAMESPACE_1);
    assertThat(dataset2InNamespace1).isNull();
    final DatasetConfigDTO dataset2InUnsetNamespace = datasetConfigDAO.findByDatasetAndNamespaceOrUnsetNamespace(DATASET_2, null);
    assertThat(dataset2InUnsetNamespace.getDataset()).isEqualTo(DATASET_2);
  }

  @Test(dependsOnMethods = {"testFindByDataset"})
  public void testUpdate() {
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findById(datasetConfigId1);
    Assert.assertNotNull(datasetConfig);
    Assert.assertFalse(datasetConfig.isRealtime());
    datasetConfig.setRealtime(true);
    datasetConfigDAO.update(datasetConfig);
    datasetConfig = datasetConfigDAO.findById(datasetConfigId1);
    Assert.assertNotNull(datasetConfig);
    Assert.assertTrue(datasetConfig.isRealtime());
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    datasetConfigDAO.deleteById(datasetConfigId2);
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findById(datasetConfigId2);
    Assert.assertNull(datasetConfig);
  }
}
