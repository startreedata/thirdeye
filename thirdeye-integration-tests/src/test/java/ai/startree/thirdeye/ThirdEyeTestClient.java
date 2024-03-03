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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.HappyPathTest.ANOMALIES_LIST_TYPE;
import static ai.startree.thirdeye.HappyPathTest.assert200;

import ai.startree.thirdeye.spi.api.AnomalyApi;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

public class ThirdEyeTestClient {

  private final Client client;
  private final int port;

  public ThirdEyeTestClient(Client client, int port) {
    this.client = client;
    this.port = port;
  }

  public Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", port, pathFragment);
  }

  public List<AnomalyApi> getAnomalies() {
    return getAnomalies("");
  }

  public List<AnomalyApi> getAnomalies(String urlSuffix) {
    final Response response = request("api/anomalies" + urlSuffix).get();
    assert200(response);
    return response.readEntity(ANOMALIES_LIST_TYPE);
  }
}
