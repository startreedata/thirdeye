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

import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRootcauseTemplateManager {

  private final static String TEMPLATE_NAME = "test_template_";
  private RootcauseTemplateManager templateDao;

  @BeforeMethod
  void beforeMethod() {
    templateDao = new TestDatabase().createInjector().getInstance(RootcauseTemplateManager.class);
  }

  @Test
  public void testSaveOrUpdate() {
    RootcauseTemplateDTO template = constructTemplate(1);
    templateDao.saveOrUpdate(template);
    List<RootcauseTemplateDTO> res1 = templateDao.findAll();
    Assert.assertEquals(res1.size(), 1);
    Assert.assertEquals(res1.get(0).getName(), TEMPLATE_NAME + 1);
    templateDao.saveOrUpdate(template);
    List<RootcauseTemplateDTO> res2 = templateDao.findAll();
    Assert.assertEquals(res2.size(), 1);
  }

  @Test
  public void testFindByMetricId() {
    RootcauseTemplateDTO template1 = constructTemplate(1);
    templateDao.save(template1);
    RootcauseTemplateDTO template2 = constructTemplate(2);
    templateDao.save(template2);
    List<RootcauseTemplateDTO> res1 = templateDao.findAll();
    Assert.assertEquals(res1.size(), 2);
    List<RootcauseTemplateDTO> res2 = templateDao.findByMetricId(1);
    Assert.assertEquals(res2.size(), 1);
    Assert.assertEquals(res2.get(0).getName(), TEMPLATE_NAME + 1);
  }

  private RootcauseTemplateDTO constructTemplate(int metricId) {
    RootcauseTemplateDTO rootcauseTemplateDTO = new RootcauseTemplateDTO();
    rootcauseTemplateDTO.setName(TEMPLATE_NAME + metricId);
    rootcauseTemplateDTO.setOwner("tester");
    rootcauseTemplateDTO.setMetricId(metricId);
    rootcauseTemplateDTO.setModules(new ArrayList<>());
    return rootcauseTemplateDTO;
  }
}
