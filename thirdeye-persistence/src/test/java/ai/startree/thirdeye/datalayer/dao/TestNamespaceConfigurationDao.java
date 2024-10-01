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
package ai.startree.thirdeye.datalayer.dao;

import static ai.startree.thirdeye.datalayer.DatalayerTestUtils.buildNamespaceConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestNamespaceConfigurationDao {

  private static final String namespace1 = "my-namespace";
  private static final String namespace2 = "my-namespace-2";

  private NamespaceConfigurationDao dao;

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    dao = injector.getInstance(NamespaceConfigurationDao.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    dao.getAll().forEach(task -> dao.delete(task.getId()));
  }

  @Test
  public void saveTest() {
    final NamespaceConfigurationDTO dto1 = buildNamespaceConfiguration(namespace1);
    final NamespaceConfigurationDTO dto2 = buildNamespaceConfiguration(namespace2);

    final Long namespaceConfigurationId1 = dao.put(dto1);
    assertThat(namespaceConfigurationId1).isGreaterThan(0L);
    dto1.setId(namespaceConfigurationId1);

    final Long namespaceConfigurationId2 = dao.put(dto2);
    assertThat(namespaceConfigurationId2).isGreaterThan(0L);
    dto2.setId(namespaceConfigurationId2);

    assertThat(dao.get(namespaceConfigurationId1)).isEqualTo(dto1);
    assertThat(dao.get(namespaceConfigurationId2)).isEqualTo(dto2);
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void updateTest() {
    final NamespaceConfigurationDTO dto = dao.getAll().get(0);
    dto.setVersion(dto.getVersion()+1);

    assertThat(dao.update(dto)).isEqualTo(1);
    assertThat(dao.get(dto.getId()).getVersion()).isEqualTo(dto.getVersion());
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void updateWithNilIDTest() {
    final NamespaceConfigurationDTO dto = buildNamespaceConfiguration(namespace1);

    try {
      dao.update(dto);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
      // left blank
    }
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void updateMultipleTest() {
    final NamespaceConfigurationDTO dto1 = dao.getAll().get(0);
    dto1.setTimeConfiguration(dto1.getTimeConfiguration()
        .setMinimumOnboardingStartTime(996684800000L));

    final NamespaceConfigurationDTO dto2 = dao.getAll().get(1);
    dto2.setVersion(dto2.getVersion()+1);

    assertThat(dao.update(Collections.emptyList())).isEqualTo(0);
    assertThat(dao.update(Arrays.asList(dto1, dto2))).isEqualTo(2);
    assertThat(dao.get(dto1.getId()).getTimeConfiguration().getMinimumOnboardingStartTime())
        .isEqualTo(dto1.getTimeConfiguration().getMinimumOnboardingStartTime());
    assertThat(dao.get(dto2.getId()).getVersion()).isEqualTo(dto2.getVersion());
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void updatePredicateTest() {
    final NamespaceConfigurationDTO dto = dao.getAll().get(0);
    dto.setVersion(dto.getVersion()+1);

    assertThat(dao.update(dto, Predicate.EQ("namespace", "some-other-namespace")))
        .isEqualTo(0);
    assertThat(dao.update(dto, Predicate.EQ("namespace", namespace1)))
        .isEqualTo(1);
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void countTest() {
    assertThat(dao.count()).isEqualTo(2);
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void countPredicateTest() {
    assertThat(dao.count(Predicate.EQ("namespace", null)))
        .isEqualTo(0);
    assertThat(dao.count(Predicate.EQ("namespace", namespace1)))
        .isEqualTo(1);
    assertThat(dao.count(Predicate.EQ("namespace", namespace2)))
        .isEqualTo(1);
  }

  @Test(dependsOnMethods = {"updateTest"})
  public void deleteTest() {
    final List<NamespaceConfigurationDTO> tasks = dao.getAll();
    assertThat(tasks.size()).isGreaterThan(0);

    final NamespaceConfigurationDTO namespaceConfigToDelete = tasks.get(0);
    dao.delete(namespaceConfigToDelete.getId());

    assertThat(dao.get(namespaceConfigToDelete.getId())).isNull();
    assertThat(dao.count()).isEqualTo(tasks.size()-1);
  }

  @Test
  public void deleteByPredicateTest() {
    final NamespaceConfigurationDTO dto = buildNamespaceConfiguration(namespace1);
    dao.put(dto);
    assertThat(dao.deleteByPredicate(Predicate.EQ("namespace", dto.namespace())))
        .isEqualTo(1);
    assertThat(dao.get(dto.getId())).isNull();
  }
}
