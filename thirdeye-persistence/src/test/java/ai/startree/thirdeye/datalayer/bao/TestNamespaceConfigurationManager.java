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

import static ai.startree.thirdeye.datalayer.DatalayerTestUtils.buildNamespaceConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import java.util.Arrays;
import java.util.Collections;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestNamespaceConfigurationManager {

  private static final String namespace1 = "my-namespace";
  private static final String namespace2 = "my-namespace-2";
  private Long namespaceConfigurationId1;
  private Long namespaceConfigurationId2;
  private NamespaceConfigurationManager namespaceConfigurationDao;

  @BeforeClass
  void beforeClass() {
    namespaceConfigurationDao = MySqlTestDatabase.sharedInjector()
        .getInstance(NamespaceConfigurationManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    namespaceConfigurationDao.findAll().forEach(namespaceConfigurationDao::delete);
  }

  @Test
  public void testGetNamespaceConfiguration() {
    final NamespaceConfigurationDTO expectedDtoNull = buildNamespaceConfiguration(null);
    expectedDtoNull.setId(1L);
    final NamespaceConfigurationDTO expectedDto1 = buildNamespaceConfiguration(namespace1);
    expectedDto1.setId(2L);
    final NamespaceConfigurationDTO expectedDto2 = buildNamespaceConfiguration(namespace2);
    expectedDto2.setId(3L);

    // fetch namespace configurations for different namespaces
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(null))
        .isEqualTo(expectedDtoNull);
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace1))
        .isEqualTo(expectedDto1);
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace2))
        .isEqualTo(expectedDto2);

    // fetch again (this time already config values will be returned)
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(null))
        .isEqualTo(expectedDtoNull);
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace1))
        .isEqualTo(expectedDto1);
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace2))
        .isEqualTo(expectedDto2);
  }

  @Test
  public void testSave() {
    final NamespaceConfigurationDTO dto1 = buildNamespaceConfiguration(namespace1);
    final NamespaceConfigurationDTO dto2 = buildNamespaceConfiguration(namespace2);

    namespaceConfigurationId1 = namespaceConfigurationDao.save(dto1);
    assertThat(namespaceConfigurationId1).isGreaterThan(0L);
    assertThat(dto1).isNotNull();

    namespaceConfigurationId2 = namespaceConfigurationDao.save(dto2);
    assertThat(namespaceConfigurationId1).isGreaterThan(0L);
    assertThat(dto2).isNotNull();
  }

  @Test(dependsOnMethods = {"testSave"})
  public void testUpdate() {
    final NamespaceConfigurationDTO dto = namespaceConfigurationDao
        .findById(namespaceConfigurationId1);
    assertThat(dto).isNotNull();
    dto.setVersion(dto.getVersion()+1);

    namespaceConfigurationDao.update(dto);
    final NamespaceConfigurationDTO updatedDto = namespaceConfigurationDao
        .findById(namespaceConfigurationId1);
    assertThat(updatedDto).isNotNull();
    assertThat(updatedDto.getVersion()).isEqualTo(dto.getVersion());
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testUpdateMultiple() {
    final NamespaceConfigurationDTO dto1 = namespaceConfigurationDao
        .findById(namespaceConfigurationId1);
    dto1.setTimeConfiguration(dto1.getTimeConfiguration()
        .setMinimumOnboardingStartTime(996684800000L));

    final NamespaceConfigurationDTO dto2 = namespaceConfigurationDao
        .findById(namespaceConfigurationId2);
    dto2.setVersion(dto2.getVersion()+1);

    assertThat(namespaceConfigurationDao.update(Collections.emptyList())).isEqualTo(0);
    assertThat(namespaceConfigurationDao.update(Arrays.asList(dto1, dto2))).isEqualTo(2);
    assertThat(namespaceConfigurationDao.findById(namespaceConfigurationId1)
        .getTimeConfiguration().getMinimumOnboardingStartTime())
        .isEqualTo(dto1.getTimeConfiguration().getMinimumOnboardingStartTime());
    assertThat(namespaceConfigurationDao.findById(namespaceConfigurationId2)
        .getVersion()).isEqualTo(dto2.getVersion());
  }

  @Test(dependsOnMethods = {"testUpdateMultiple"})
  public void testDelete() {
    final NamespaceConfigurationDTO dto1 = namespaceConfigurationDao
        .findById(namespaceConfigurationId1);
    assertThat(dto1).isNotNull();

    namespaceConfigurationDao.delete(dto1);
    assertThat(namespaceConfigurationDao.findById(namespaceConfigurationId1)).isNull();
  }

  @Test(dependsOnMethods = {"testDelete"})
  public void testDeleteById() {
    assertThat(namespaceConfigurationDao.findById(namespaceConfigurationId2)).isNotNull();

    namespaceConfigurationDao.deleteById(namespaceConfigurationId2);
    assertThat(namespaceConfigurationDao.findById(namespaceConfigurationId2)).isNull();
  }
}