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

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Duration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.ws.rs.client.Client;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

public class DropwizardTestUtils {

  private static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  public static DropwizardTestSupport<ThirdEyeServerConfiguration> buildSupport(
      final DatabaseConfiguration dbConfiguration, final String serverConfigPath) {
    return new DropwizardTestSupport<>(ThirdEyeServer.class, resourceFilePath(serverConfigPath),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", dbConfiguration.getUrl()),
        config("database.user", dbConfiguration.getUser()),
        config("database.password", dbConfiguration.getPassword()),
        config("database.driver", dbConfiguration.getDriver()));
  }

  public static Client buildClient(final String clientName,
      DropwizardTestSupport<ThirdEyeServerConfiguration> support) {
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(Duration.minutes(1)); // for timeout issues
    return new JerseyClientBuilder(support.getEnvironment()).using(jerseyClientConfiguration)
        .build(clientName);
  }

  public static AlertApi loadAlertApi(final String alertJsonPath) throws IOException {
    String alertApiJson = IOUtils.resourceToString(alertJsonPath, StandardCharsets.UTF_8);
    return OBJECT_MAPPER.readValue(alertApiJson, AlertApi.class);
  }

  public static AlertEvaluationApi alertEvaluationApi(final AlertApi alertApi, final long startTime,
      final long endTime) {
    return new AlertEvaluationApi()
        .setAlert(alertApi)
        .setStart(Date.from(
            Instant.ofEpochMilli(startTime))) //Sunday, 2 February 2020 00:00:00
        .setEnd(Date.from(Instant.ofEpochMilli(endTime)));//Sunday, 2 August 2020 00:00:00
  }
}
