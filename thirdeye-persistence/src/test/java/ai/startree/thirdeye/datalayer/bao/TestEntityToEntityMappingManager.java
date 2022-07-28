/*
 * Copyright 2022 StarTree Inc
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
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestEntityToEntityMappingManager {

  private static final String METRIC_TO_METRIC = "METRIC_TO_METRIC";
  private static final String METRIC_TO_SERVICE = "METRIC_TO_SERVICE";
  private static final String DIMENSION_TO_DIMENSION = "DIMENSION_TO_DIMENSION";

  Long testId1 = null;
  Long testId2 = null;
  Long testId3 = null;

  String metricURN1 = "thirdeye:metric:d1:m1";
  String metricURN2 = "thirdeye:metric:d1:m2";
  String serviceURN1 = "thirdeye:service:s1";
  String dimensionURN1 = "thirdeye:dimension:country:UnitedStates";
  String dimensionURN2 = "thirdeye:dimension:country:US";

  private EntityToEntityMappingManager entityToEntityMappingDAO;

  @BeforeClass
  void beforeClass() {
    entityToEntityMappingDAO = new MySqlTestDatabase()
        .createInjector()
        .getInstance(EntityToEntityMappingManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test
  public void testCreate() {
    EntityToEntityMappingDTO dto = DatalayerTestUtils.getTestEntityToEntityMapping(metricURN1,
        metricURN2,
        METRIC_TO_METRIC);
    testId1 = entityToEntityMappingDAO.save(dto);
    Assert.assertNotNull(testId1);
    dto = DatalayerTestUtils.getTestEntityToEntityMapping(metricURN1,
        serviceURN1,
        METRIC_TO_SERVICE);
    testId2 = entityToEntityMappingDAO.save(dto);
    Assert.assertNotNull(testId2);
    dto = DatalayerTestUtils.getTestEntityToEntityMapping(dimensionURN1,
        dimensionURN2,
        DIMENSION_TO_DIMENSION);
    testId3 = entityToEntityMappingDAO.save(dto);
    Assert.assertNotNull(testId3);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFind() {
    EntityToEntityMappingDTO dto = entityToEntityMappingDAO.findById(testId1);
    Assert.assertEquals(dto.getId(), testId1);
    Assert.assertEquals(dto.getFromURN(), metricURN1);
    Assert.assertEquals(dto.getToURN(), metricURN2);
    Assert.assertEquals(dto.getMappingType(), METRIC_TO_METRIC);

    List<EntityToEntityMappingDTO> list = entityToEntityMappingDAO.findAll();
    Assert.assertEquals(list.size(), 3);

    list = entityToEntityMappingDAO.findByFromURN(metricURN1);
    Assert.assertEquals(list.size(), 2);

    list = entityToEntityMappingDAO.findByToURN(metricURN2);
    Assert.assertEquals(list.size(), 1);

    dto = entityToEntityMappingDAO.findByFromAndToURN(dimensionURN1, dimensionURN2);
    Assert.assertEquals(dto.getId(), testId3);

    list = entityToEntityMappingDAO.findByMappingType(METRIC_TO_SERVICE);
    Assert.assertEquals(list.size(), 1);

    list = entityToEntityMappingDAO.findByFromURNAndMappingType(metricURN1, METRIC_TO_METRIC);
    Assert.assertEquals(list.size(), 1);

    list = entityToEntityMappingDAO.findByToURNAndMappingType(dimensionURN2, METRIC_TO_METRIC);
    Assert.assertEquals(list.size(), 0);
  }

  @Test(dependsOnMethods = {"testFind"})
  public void testUpdate() {
    EntityToEntityMappingDTO dto = entityToEntityMappingDAO.findById(testId1);
    Assert.assertEquals(dto.getScore(), 1d);
    dto.setScore(10);
    entityToEntityMappingDAO.update(dto);
    dto = entityToEntityMappingDAO.findById(testId1);
    Assert.assertEquals(dto.getScore(), 10d);
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    entityToEntityMappingDAO.deleteById(testId1);
    EntityToEntityMappingDTO dto = entityToEntityMappingDAO.findById(testId1);
    Assert.assertNull(dto);
  }
}
