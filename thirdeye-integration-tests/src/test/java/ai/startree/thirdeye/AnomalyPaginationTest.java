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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_LIMIT_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_OFFSET_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OFFSET_WITHOUT_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import io.dropwizard.testing.DropwizardTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Use this for testing the bits related to pagination
 */
public class AnomalyPaginationTest {

  private static final Logger log = LoggerFactory.getLogger(AnomalyPaginationTest.class);
  private static final GenericType<List<AnomalyApi>> ANOMALY_LIST_TYPE = new GenericType<>() {};
  private static final GenericType<StatusListApi> ERROR_LIST_TYPE = new GenericType<>() {};
  private static final int TOTAL_ANOMALIES = 100;
  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  private static AnomalyApi anomaly() {
    return new AnomalyApi();
  }

  private static List<Long> apisToIds(final List<AnomalyApi> apis) {
    return apis.stream()
        .map(ThirdEyeCrudApi::getId)
        .collect(Collectors.toList());
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    SUPPORT = buildSupport(dbConfiguration, "happypath/config/server.yaml");
    SUPPORT.before();
    client = buildClient("pagination-test-client", SUPPORT);

    final List<AnomalyApi> anomalies = new ArrayList<>();
    for (int i = 0; i < TOTAL_ANOMALIES; i++) {
      anomalies.add(anomaly());
    }
    request("api/anomalies").post(Entity.json(anomalies));
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    log.info("Stopping Thirdeye at port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    MySqlTestDatabase.cleanSharedDatabase();
  }

  @Test
  public void testResponseSizeWithLimitFilter() {
    final int limit = 20;
    Response response = request("api/anomalies?limit=" + limit).get();
    assertThat(response.getStatus()).isEqualTo(200);
    List<AnomalyApi> returnedAnomalies = response.readEntity(ANOMALY_LIST_TYPE);
    assertThat(returnedAnomalies.size()).isEqualTo(limit);

    // ensure when limit is higher that actual entity cardinality, it returns all the entries
    response = request("api/anomalies?limit=" + (limit + TOTAL_ANOMALIES)).get();
    assertThat(response.getStatus()).isEqualTo(200);
    returnedAnomalies = response.readEntity(ANOMALY_LIST_TYPE);
    assertThat(returnedAnomalies.size()).isEqualTo(TOTAL_ANOMALIES);
  }

  @Test
  public void testResponseWithLimitAndOffsetFilters() {
    final int limit = 40;
    int offset = 0;
    final List<Long> allPagesIds = new ArrayList<>();

    while (offset < TOTAL_ANOMALIES) {
      final List<AnomalyApi> page = getAllWithLimitAndOffset(limit, offset);
      allPagesIds.addAll(apisToIds(page));
      // limit -> for all but the last page
      // TOTAL_ANOMALIES - offset -> for the last page
      assertThat(page.size()).isEqualTo(Math.min(limit, TOTAL_ANOMALIES - offset));
      // increment the page
      offset += limit;
    }

    final Response response = request("api/anomalies").get();
    final List<AnomalyApi> getAll = response.readEntity(ANOMALY_LIST_TYPE);

    assertThat(allPagesIds.size()).isEqualTo(getAll.size());
    getAll.forEach(entry -> assertThat(allPagesIds.contains(entry.getId())).isTrue());
  }

  @Test
  public void testNegativeLimitValue() {
    final Response response = request("api/anomalies?limit=-10").get();
    final StatusListApi results = response.readEntity(ERROR_LIST_TYPE);
    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(results.getList()).isNotEmpty();
    assertThat(results.getList().get(0).getCode()).isEqualTo(ERR_NEGATIVE_LIMIT_VALUE);
  }

  @Test
  public void testNegativeOffsetValue() {
    final Response response = request("api/anomalies?limit=5&offset=-10").get();
    final StatusListApi results = response.readEntity(ERROR_LIST_TYPE);
    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(results.getList()).isNotEmpty();
    assertThat(results.getList().get(0).getCode()).isEqualTo(ERR_NEGATIVE_OFFSET_VALUE);
  }

  @Test
  public void testOffsetWithoutLimit() {
    final Response response = request("api/anomalies?offset=10").get();
    final StatusListApi results = response.readEntity(ERROR_LIST_TYPE);
    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(results.getList()).isNotEmpty();
    assertThat(results.getList().get(0).getCode()).isEqualTo(ERR_OFFSET_WITHOUT_LIMIT);
  }

  @Test
  public void testLimitAndOffsetWithOtherFilters() {
    Response response = request("api/anomalies?isChild=true&limit=5&offset=5").get();
    List<AnomalyApi> results = response.readEntity(ANOMALY_LIST_TYPE);
    assertThat(results).isEmpty();

    response = request("api/anomalies?isChild=false&limit=5&offset=5").get();
    results = response.readEntity(ANOMALY_LIST_TYPE);
    assertThat(results.size()).isEqualTo(5);
  }

  private List<AnomalyApi> getAllWithLimitAndOffset(final int limit, final int offset) {
    final Response response = request(String.format("api/anomalies?limit=%s&offset=%s",
        limit,
        offset)).get();
    return response.readEntity(ANOMALY_LIST_TYPE);
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }
}
