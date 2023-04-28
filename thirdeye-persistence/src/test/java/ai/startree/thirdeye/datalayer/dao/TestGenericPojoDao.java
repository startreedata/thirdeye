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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_LIMIT_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_OFFSET_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OFFSET_WITHOUT_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import java.util.List;
import java.util.Random;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestGenericPojoDao {

  private static final String NAME = "name";
  private static final String BASE_ID = "baseId";
  private static final String TYPE = "type";
  private static final String VERSION = "version";
  private static final List<String> TEST_NAMES = List.of("test1", "test2");
  private static final List<String> TEST_TYPES = List.of("type", "test");
  private static final int TOTAL_ANOMALIES = 100;

  private GenericPojoDao dao;

  private static long getRandomLimit() {
    // avoid limit as 0
    return new Random().nextInt(TOTAL_ANOMALIES-1)+1;
  }

  private static AnomalyDTO anomaly() {
    return new AnomalyDTO();
  }

  @BeforeClass
  void beforeClass() {
    dao = MySqlTestDatabase.sharedInjector().getInstance(GenericPojoDao.class);
    for(int i=0; i<TOTAL_ANOMALIES; i++) {
      dao.create(anomaly());
    }
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    // clean up entries created during the tests
    dao.deleteByPredicate(Predicate.NEQ(NAME, "null"), DataSourceDTO.class);
    dao.deleteByPredicate(Predicate.NEQ(BASE_ID, 0), AnomalyDTO.class);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSaveEntityWithId() {
    dao.create((new DataSourceDTO()).setId(12345L));
  }

  @Test
  public void saveEntityTest() {
    final DataSourceDTO dto1 = new DataSourceDTO()
        .setName(TEST_NAMES.get(0))
        .setType(TEST_TYPES.get(0));
    final DataSourceDTO dto2 = new DataSourceDTO()
        .setName(TEST_NAMES.get(1))
        .setType(TEST_TYPES.get(0));
    final Long id1 = dao.create(dto1);
    final Long id2 = dao.create(dto2);

    assertThat(dao.get(id1, DataSourceDTO.class).getName()).isEqualTo(TEST_NAMES.get(0));
    assertThat(dao.get(id2, DataSourceDTO.class).getName()).isEqualTo(TEST_NAMES.get(1));

    // On insertion of duplicate entry (same name), it should not be inserted
    final DataSourceDTO dto3 = new DataSourceDTO()
        .setName(dto1.getName())
        .setType(dto1.getType());
    final Long id3 = dao.create(dto3);
    // should return a null id
    assertThat(id3).isNull();
    // generic table must also have only 2 entries
    assertThat(dao.getAll(DataSourceDTO.class).size()).isEqualTo(2);
  }

  @Test(dependsOnMethods = "saveEntityTest", timeOut = 60000L)
  public void updateEntityTest() {
    final List<DataSourceDTO> dtos = dao.get(Predicate.EQ(TYPE, TEST_TYPES.get(0)), DataSourceDTO.class);
    assertThat(dtos.size()).isEqualTo(2);
    final DataSourceDTO dto = dtos.get(0);
    dto.setType(TEST_TYPES.get(1));
    final long idBeforeUpdate = dto.getId();
    final int rowsUpdated = dao.update(dto, Predicate.EQ(VERSION, 1));
    // only 1 row must be updated
    assertThat(rowsUpdated).isEqualTo(1);
    final List<DataSourceDTO> dtoAfterUpdate = dao.get(Predicate.EQ(TYPE, TEST_TYPES.get(1)),
        DataSourceDTO.class);
    assertThat(dtoAfterUpdate.size()).isEqualTo(1);
    assertThat(dtoAfterUpdate.get(0).getId()).isEqualTo(idBeforeUpdate);
  }

  @Test(dependsOnMethods = "updateEntityTest", timeOut = 60000L)
  public void deleteEntityTest() {
    final int deletedEntries = dao.deleteByPredicate(
        Predicate.IN(NAME, TEST_NAMES.toArray()),
        DataSourceDTO.class);
    assertThat(deletedEntries).isEqualTo(2);
    assertThat(dao.getAll(DataSourceDTO.class).size()).isEqualTo(0);
  }

  @Test
  public void filterWithLimitTest() {
    final long limit = getRandomLimit();
    final DaoFilter filter = new DaoFilter()
        .setLimit(limit)
        .setBeanClass(AnomalyDTO.class);
    final List<AnomalyDTO> anomalies = dao.filter(filter);
    assertThat(anomalies).isNotNull();
    assertThat(anomalies.size()).isEqualTo(limit);
  }

  @Test
  public void filterWithLimitAndOffsetTest() {
    final long limit = getRandomLimit();
    // ensure last page entries are fetched
    final long offset = TOTAL_ANOMALIES - (TOTAL_ANOMALIES % limit);
    final DaoFilter filter = new DaoFilter()
        .setLimit(limit)
        .setOffset(offset)
        .setBeanClass(AnomalyDTO.class);
    final List<AnomalyDTO> anomalies = dao.filter(filter);
    assertThat(anomalies).isNotNull();
    assertThat(anomalies.size()).isEqualTo(TOTAL_ANOMALIES - offset);
  }

  @Test
  public void filterWithOffsetWithoutLimitTest() {
    final long offset = TOTAL_ANOMALIES/2;
    final DaoFilter filter = new DaoFilter()
        .setOffset(offset)
        .setBeanClass(AnomalyDTO.class);
    assertThatThrownBy(() -> dao.filter(filter))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERR_OFFSET_WITHOUT_LIMIT.getMessage());
  }

  @Test
  public void testNegativeLimitValue() {
    final DaoFilter filter = new DaoFilter()
        .setLimit(-5L)
        .setBeanClass(AnomalyDTO.class);
    assertThatThrownBy(() -> dao.filter(filter))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERR_NEGATIVE_LIMIT_VALUE.getMessage());
  }

  @Test
  public void testNegativeOffsetValue() {
    final DaoFilter filter = new DaoFilter()
        .setLimit((long) TOTAL_ANOMALIES)
        .setOffset(-5L)
        .setBeanClass(AnomalyDTO.class);
    assertThatThrownBy(() -> dao.filter(filter))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERR_NEGATIVE_OFFSET_VALUE.getMessage());
  }

  @Test
  public void pageBoundariesTest() {
    // ensure more than 2 pages are formed
    final long limit = TOTAL_ANOMALIES/3;
    long offset = 0;
    long entryCount = 0;
    final DaoFilter filter = new DaoFilter()
        .setBeanClass(AnomalyDTO.class)
        .setLimit(limit);

    while (offset < TOTAL_ANOMALIES) {
      filter.setOffset(offset);
      final List<AnomalyDTO> anomalies = dao.filter(filter);
      assertThat(anomalies).isNotNull();
      entryCount += anomalies.size();
      // limit -> for all but the last page
      // TOTAL_ANOMALIES - offset -> for the last page
      assertThat(anomalies.size()).isEqualTo(Math.min(limit, TOTAL_ANOMALIES - offset));
      // increment the page
      offset += limit;
    }
    assertThat(entryCount).isEqualTo(TOTAL_ANOMALIES);
  }
}
