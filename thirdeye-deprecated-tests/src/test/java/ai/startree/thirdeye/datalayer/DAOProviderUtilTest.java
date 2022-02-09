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
