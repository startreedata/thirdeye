/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.restclient;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestThirdEyeRcaRestClient {

  private TestDbEnv testDAOProvider;
  private long anomalyId;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    this.testDAOProvider = new TestDbEnv();
    DAORegistry daoRegistry = TestDbEnv.getInstance();

    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setCollection("collection");
    anomaly.setMetric("metric");
    anomalyId = daoRegistry.getMergedAnomalyResultDAO().save(anomaly);
  }

  @Test
  public void testGetAllHighlights() throws Exception {
    Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("cubeResults", new HashMap<>());

    Client client = MockAbstractRestClient.setupMockClient(expectedResponse);

    ThirdEyePrincipal principal = new ThirdEyePrincipal(null, "dummy");
    ThirdEyeRcaRestClient rcaClient = new ThirdEyeRcaRestClient(client, principal);
    Map<String, Object> result = rcaClient.getRootCauseHighlights(anomalyId);

    Assert.assertTrue(result.containsKey("cubeResults"));
    Assert.assertEquals(expectedResponse.get("cubeResults"), result.get("cubeResults"));
  }
}
