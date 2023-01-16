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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ForkJoinEvaluationApiTest {

  private static final String RESOURCES_PATH = "/forkJoinEvaluationApiTest";
  private static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  private static AlertEvaluationApi getAlertEvaluationApi(final String filename) {
    final String path = String.format("%s/%s", RESOURCES_PATH, filename);
    final AlertEvaluationApi alertEvaluationApi;
    try {
      final String alertEvaluationApiJson = IOUtils.resourceToString(path,
          StandardCharsets.UTF_8);
      alertEvaluationApi = OBJECT_MAPPER.readValue(alertEvaluationApiJson,
          AlertEvaluationApi.class);
    } catch (final IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
    return alertEvaluationApi;
  }

  private Client client;

  @BeforeClass
  public void beforeClass() {
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues

    client = new JerseyClientBuilder(new MetricRegistry())
        .using(jerseyClientConfiguration)
        .using(OBJECT_MAPPER)
        .using(Executors.newSingleThreadExecutor())
        .build("test client");
  }

  @Test(enabled = false)
  public void testDimensionExplorationNoVariables() {
    final AlertEvaluationApi alertEvaluationApi = getAlertEvaluationApi(
        "dimension-exploration-no-variables.json");

    final Response response = client.target("http://localhost:8080/api/alerts/evaluate")
        .request()
        .post(Entity.json(alertEvaluationApi));

    assertThat(response.getStatus()).isEqualTo(200);

    final AlertEvaluationApi result = response.readEntity(AlertEvaluationApi.class);

    final Map<String, DetectionEvaluationApi> detectionEvaluations = result.getDetectionEvaluations();
    assertThat(detectionEvaluations.size()).isEqualTo(3);
    for (DetectionEvaluationApi e : detectionEvaluations.values()) {
      assertThat(e.getAnomalies().size()).isEqualTo(1);
    }
  }


  @Test(enabled = false)
  public void testDimensionExplorationWithVariable() {
    final AlertEvaluationApi alertEvaluationApi = getAlertEvaluationApi(
        "dimension-exploration-with-variable.json");

    final Response response = client.target("http://localhost:8080/api/alerts/evaluate")
        .request()
        .post(Entity.json(alertEvaluationApi));

    assertThat(response.getStatus()).isEqualTo(200);

    final AlertEvaluationApi result = response.readEntity(AlertEvaluationApi.class);

    final Map<String, DetectionEvaluationApi> detectionEvaluations = result.getDetectionEvaluations();
    assertThat(detectionEvaluations.size()).isEqualTo(2);

    final Set<Integer> anomalyCounts = detectionEvaluations.values()
        .stream()
        .map(e -> e.getAnomalies().size())
        .collect(Collectors.toSet());

    assertThat(anomalyCounts).isEqualTo(Set.of(0, 1));
  }

  @Test(enabled = false)
  public void testDimensionExplorationWithTwoVariables() {
    final AlertEvaluationApi alertEvaluationApi = getAlertEvaluationApi(
        "dimension-exploration-with-2variables.json");

    final Response response = client.target("http://localhost:8080/api/alerts/evaluate")
        .request()
        .post(Entity.json(alertEvaluationApi));

    assertThat(response.getStatus()).isEqualTo(200);

    final AlertEvaluationApi result = response.readEntity(AlertEvaluationApi.class);

    final Map<String, DetectionEvaluationApi> detectionEvaluations = result.getDetectionEvaluations();
    assertThat(detectionEvaluations.size()).isEqualTo(3);

    final Set<Integer> anomalyCounts = detectionEvaluations.values()
        .stream()
        .map(e -> e.getAnomalies().size())
        .collect(Collectors.toSet());

    assertThat(anomalyCounts).isEqualTo(Set.of(0, 1, 26));
  }
}
