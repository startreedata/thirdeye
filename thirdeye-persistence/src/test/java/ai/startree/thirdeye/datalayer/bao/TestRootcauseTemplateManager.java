/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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

  private RootcauseTemplateManager templateDao;
  private final static String TEMPLATE_NAME = "test_template_";
  private final static String APPLICATION_NAME = "test-app";

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
    Assert.assertEquals(res1.get(0).getApplication(), APPLICATION_NAME);
    template.setApplication(APPLICATION_NAME + "-1");
    templateDao.saveOrUpdate(template);
    List<RootcauseTemplateDTO> res2 = templateDao.findAll();
    Assert.assertEquals(res2.size(), 1);
    Assert.assertEquals(res2.get(0).getApplication(), APPLICATION_NAME + "-1");
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
    rootcauseTemplateDTO.setApplication(APPLICATION_NAME);
    rootcauseTemplateDTO.setMetricId(metricId);
    rootcauseTemplateDTO.setModules(new ArrayList<>());
    return rootcauseTemplateDTO;
  }
}
