/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.DAORegistry;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DAOProviderUtilTest {

  private TestDbEnv testDAOProvider;

  @BeforeClass
  public void beforeClass() {
    testDAOProvider = new TestDbEnv();
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    testDAOProvider.cleanup();
  }

  @Test
  public void testProviderReturnsSameInstance() {
    DAORegistry daoRegistry = TestDbEnv.getInstance();
    MergedAnomalyResultManager m1 = daoRegistry.getMergedAnomalyResultDAO();
    MergedAnomalyResultManager m2 = daoRegistry.getMergedAnomalyResultDAO();
    Assert.assertSame(m1, m2);
  }
}
