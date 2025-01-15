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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestNamespaceConfigurationManager {

  private static final String namespace1 = "my-namespace";
  private static final String namespace2 = "my-namespace-2";
  private static final String namespace3 = "my-namespace-3";
  private static final String namespace4 = "my-namespace-4";
  private static final String namespace5 = "my-namespace-5";
  private static final String namespace6 = "my-namespace-6";
  private static final String namespace7 = "my-namespace-7";
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
    // fetch namespace configurations for different namespaces
    final NamespaceConfigurationDTO dto = namespaceConfigurationDao
        .getNamespaceConfiguration(namespace3);
    assertThat(dto.getTimeConfiguration()).isNotNull();
    assertThat(dto.namespace()).isEqualTo(namespace3);

    final NamespaceConfigurationDTO dto1 = namespaceConfigurationDao
        .getNamespaceConfiguration(namespace4);
    assertThat(dto1.getTimeConfiguration()).isNotNull();
    assertThat(dto1.namespace()).isEqualTo(namespace4);

    // fetch again (this time already config values will be returned)
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace3)).isEqualTo(dto);
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace4)).isEqualTo(dto1);
  }

  @Test
  public void testUpdateNamespaceConfiguration() {
    // fetch/create new namespace configuration
    final NamespaceConfigurationDTO dto = namespaceConfigurationDao
        .getNamespaceConfiguration(namespace5);
    assertThat(dto.getTimeConfiguration()).isNotNull();
    assertThat(dto.namespace()).isEqualTo(namespace5);

    // update configuration
    dto.setTimeConfiguration(dto.getTimeConfiguration().setMinimumOnboardingStartTime(0L));
    final NamespaceConfigurationDTO updatedDto = namespaceConfigurationDao
        .updateNamespaceConfiguration(dto);
    compareDtos(updatedDto, dto);

    // fetch again (this time already updated config will be returned)
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace5))
        .isEqualTo(updatedDto);
  }

  @Test
  public void testUpdateNamespaceConfigurationWhenExistingConfigDoesntExist() {
    assertThatThrownBy(() -> namespaceConfigurationDao.updateNamespaceConfiguration(buildNamespaceConfiguration(null)))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void testResetNamespaceConfigurationIfExists() {
    // fetch/create new namespace configuration
    final NamespaceConfigurationDTO dto = namespaceConfigurationDao
        .getNamespaceConfiguration(namespace6);
    assertThat(dto.getTimeConfiguration()).isNotNull();
    assertThat(dto.namespace()).isEqualTo(namespace6);

    // update configuration
    dto.setTimeConfiguration(dto.getTimeConfiguration().setMinimumOnboardingStartTime(0L));
    final NamespaceConfigurationDTO updatedDto = namespaceConfigurationDao
        .updateNamespaceConfiguration(dto);
    compareDtos(updatedDto, dto);

    // fetch again (this time already updated config will be returned)
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace6))
        .isEqualTo(updatedDto);

    // reset configuration
    final NamespaceConfigurationDTO resettedDto = namespaceConfigurationDao
        .resetNamespaceConfiguration(namespace6);
    dto.setTimeConfiguration(buildNamespaceConfiguration(namespace6).getTimeConfiguration());
    compareDtos(resettedDto, dto);

    // fetch again
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace6)).isEqualTo(dto);
  }

  @Test
  public void testResetNamespaceConfigurationIfDoesntExists() {
    // reset configuration directly for namespace which doesn't have existing config
    final NamespaceConfigurationDTO dto = buildNamespaceConfiguration(namespace7);
    final NamespaceConfigurationDTO resettedDto = namespaceConfigurationDao
        .resetNamespaceConfiguration(namespace7);
    dto.setId(resettedDto.getId());
    compareDtos(resettedDto, dto);

    // fetch again
    assertThat(namespaceConfigurationDao.getNamespaceConfiguration(namespace7))
        .isEqualTo(resettedDto);
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

  private void compareDtos(NamespaceConfigurationDTO dto1, NamespaceConfigurationDTO dto2) {
    assertThat(dto1.getId()).isEqualTo(dto2.getId());
    assertThat(dto1.getAuth().getNamespace()).isEqualTo(dto2.getAuth().getNamespace());
    assertThat(dto1.getTimeConfiguration().getDateTimePattern())
        .isEqualTo(dto2.getTimeConfiguration().getDateTimePattern());
    assertThat(dto1.getTimeConfiguration().getTimezone())
        .isEqualTo(dto2.getTimeConfiguration().getTimezone());
    assertThat(dto1.getTimeConfiguration().getMinimumOnboardingStartTime())
        .isEqualTo(dto2.getTimeConfiguration().getMinimumOnboardingStartTime());
    assertThat(dto1.getTemplateConfiguration().getSqlLimitStatement())
        .isEqualTo(dto2.getTemplateConfiguration().getSqlLimitStatement());
    assertThat(dto1.getTaskQuotasConfiguration().getDetectionTaskQuota())
        .isEqualTo(dto2.getTaskQuotasConfiguration().getDetectionTaskQuota());
    assertThat(dto1.getTaskQuotasConfiguration().getNotificationTaskQuota())
        .isEqualTo(dto2.getTaskQuotasConfiguration().getNotificationTaskQuota());
  }
}
