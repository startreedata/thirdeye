/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.SCALING_FACTOR;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestOverrideConfigManager {

  private final DateTime now = new DateTime();
  private Long overrideConfigId1 = null;
  private OverrideConfigManager overrideConfigDAO;

  @BeforeClass
  void beforeClass() {
    overrideConfigDAO = new TestDatabase()
        .createInjector()
        .getInstance(OverrideConfigManager.class);
  }

  @Test
  public void testCreate() {
    OverrideConfigDTO overrideConfigDTO1 = DatalayerTestUtils.getTestOverrideConfigForTimeSeries(now);
    overrideConfigId1 = overrideConfigDAO.save(overrideConfigDTO1);
    Assert.assertNotNull(overrideConfigId1);
    Assert.assertNotNull(overrideConfigDAO.findById(overrideConfigId1));
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFind() {
    List<OverrideConfigDTO> overrideConfigDTOs = overrideConfigDAO.findAll();
    Assert.assertEquals(overrideConfigDTOs.size(), 1);

    overrideConfigDTOs = overrideConfigDAO
        .findAllConflictByTargetType(OverrideConfigManager.ENTITY_TIME_SERIES,
            now.minusHours(5).getMillis(), now.minusHours(2).getMillis());
    Assert.assertEquals(overrideConfigDTOs.size(), 1);

    overrideConfigDTOs = overrideConfigDAO
        .findAllConflictByTargetType(OverrideConfigManager.ENTITY_ALERT_FILTER,
            now.minusHours(5).getMillis(), now.minusHours(2).getMillis());
    Assert.assertEquals(overrideConfigDTOs.size(), 0);

    overrideConfigDTOs = overrideConfigDAO
        .findAllConflictByTargetType(OverrideConfigManager.ENTITY_TIME_SERIES,
            now.minusDays(100).getMillis(), now.minusDays(50).getMillis());
    Assert.assertEquals(overrideConfigDTOs.size(), 0);
  }

  @Test(dependsOnMethods = {"testFind"})
  public void testUpdate() {
    // Test update of override property
    OverrideConfigDTO overrideConfigDTO = overrideConfigDAO.findById(overrideConfigId1);
    Assert.assertNotNull(overrideConfigDTO);
    Assert.assertNotNull(overrideConfigDTO.getOverrideProperties());
    Assert
        .assertNotNull(overrideConfigDTO.getOverrideProperties().get(SCALING_FACTOR));
    Assert.assertEquals(overrideConfigDTO.getOverrideProperties().get(SCALING_FACTOR),
        "1.2");
    Map<String, String> newOverrideProperties = new HashMap<>();
    newOverrideProperties.put(SCALING_FACTOR, "0.8");
    overrideConfigDTO.setOverrideProperties(newOverrideProperties);
    overrideConfigDAO.update(overrideConfigDTO);

    overrideConfigDTO = overrideConfigDAO.findById(overrideConfigId1);
    Assert.assertNotNull(overrideConfigDTO);
    Assert.assertNotNull(overrideConfigDTO.getOverrideProperties());
    Assert
        .assertNotNull(overrideConfigDTO.getOverrideProperties().get(SCALING_FACTOR));
    Assert.assertEquals(overrideConfigDTO.getOverrideProperties().get(SCALING_FACTOR),
        "0.8");

    // Test update of target level
    overrideConfigDTO = overrideConfigDAO.findById(overrideConfigId1);
    Assert.assertNotNull(overrideConfigDTO);
    Assert.assertNotNull(overrideConfigDTO.getTargetLevel());
    List<String> targetLevel = overrideConfigDTO.getTargetLevel().get(OverrideConfigManager
        .TARGET_COLLECTION);
    Assert.assertNotNull(targetLevel);
    Assert.assertEquals(targetLevel, Arrays.asList("collection1", "collection2"));
    List<String> newTargetLevel = Arrays.asList("collection1", "collection4");
    overrideConfigDTO.getTargetLevel().put(OverrideConfigManager.TARGET_COLLECTION, newTargetLevel);
    overrideConfigDAO.update(overrideConfigDTO);

    overrideConfigDTO = overrideConfigDAO.findById(overrideConfigId1);
    Assert.assertNotNull(overrideConfigDTO);
    Assert.assertNotNull(overrideConfigDTO.getTargetLevel());
    targetLevel = overrideConfigDTO.getTargetLevel().get(OverrideConfigManager.TARGET_COLLECTION);
    Assert.assertNotNull(targetLevel);
    Assert.assertEquals(targetLevel, Arrays.asList("collection1", "collection4"));
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    overrideConfigDAO.deleteById(overrideConfigId1);
    OverrideConfigDTO overrideConfigDTO = overrideConfigDAO.findById(overrideConfigId1);
    Assert.assertNull(overrideConfigDTO);
  }
}
