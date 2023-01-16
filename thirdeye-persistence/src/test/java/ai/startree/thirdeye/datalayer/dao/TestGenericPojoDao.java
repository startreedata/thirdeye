/*
 * Copyright 2023 StarTree Inc
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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestGenericPojoDao {

  private static final String NAME = "name";
  private static final String TYPE = "type";
  private static final String VERSION = "version";
  private static final List<String> TEST_NAMES = List.of("test1", "test2");
  private static final List<String> TEST_TYPES = List.of("type", "test");

  private GenericPojoDao dao;

  @BeforeClass
  void beforeClass() {
    dao = MySqlTestDatabase.sharedInjector().getInstance(GenericPojoDao.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    dao.deleteByPredicate(Predicate.NEQ(NAME, "null"), DataSourceDTO.class);
  }

  @Test
  public void saveEntityTest() {
    assertThat(dao.put((new DataSourceDTO()).setId(12345L))).isEqualTo(null);

    DataSourceDTO dto1 = new DataSourceDTO()
        .setName(TEST_NAMES.get(0))
        .setType(TEST_TYPES.get(0));
    DataSourceDTO dto2 = new DataSourceDTO()
        .setName(TEST_NAMES.get(1))
        .setType(TEST_TYPES.get(0));
    Long id1 = dao.put(dto1);
    Long id2 = dao.put(dto2);

    assertThat(dao.get(id1, DataSourceDTO.class).getName()).isEqualTo(TEST_NAMES.get(0));
    assertThat(dao.get(id2, DataSourceDTO.class).getName()).isEqualTo(TEST_NAMES.get(1));

    // On insertion of duplicate entry (same name), it should not be inserted
    DataSourceDTO dto3 = new DataSourceDTO()
        .setName(dto1.getName())
        .setType(dto1.getType());
    Long id3 = dao.put(dto3);
    // should return a null id
    assertThat(id3).isNull();
    // generic table must also have only 2 entries
    assertThat(dao.getAll(DataSourceDTO.class).size()).isEqualTo(2);
  }

  @Test(dependsOnMethods = "saveEntityTest", timeOut = 60000L)
  public void updateEntityTest() {
    List<DataSourceDTO> dtos = dao.get(Predicate.EQ(TYPE, TEST_TYPES.get(0)), DataSourceDTO.class);
    assertThat(dtos.size()).isEqualTo(2);
    DataSourceDTO dto = dtos.get(0);
    dto.setType(TEST_TYPES.get(1));
    long idBeforeUpdate = dto.getId();
    int rowsUpdated = dao.update(dto, Predicate.EQ(VERSION, 1));
    // only 1 row must be updated
    assertThat(rowsUpdated).isEqualTo(1);
    List<DataSourceDTO> dtoAfterUpdate = dao.get(Predicate.EQ(TYPE, TEST_TYPES.get(1)), DataSourceDTO.class);
    assertThat(dtoAfterUpdate.size()).isEqualTo(1);
    assertThat(dtoAfterUpdate.get(0).getId()).isEqualTo(idBeforeUpdate);
  }

  @Test(dependsOnMethods = "updateEntityTest", timeOut = 60000L)
  public void deleteEntityTest() {
    int deletedEntries = dao.deleteByPredicate(
        Predicate.IN(NAME, TEST_NAMES.toArray()),
        DataSourceDTO.class);
    assertThat(deletedEntries).isEqualTo(2);
    assertThat(dao.getAll(DataSourceDTO.class).size()).isEqualTo(0);
  }

}
