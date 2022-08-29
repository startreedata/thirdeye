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
package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.IntegrationTestUtils.getJWKS;
import static ai.startree.thirdeye.IntegrationTestUtils.getToken;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.ThirdEyeServer;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuthIntegrationTest {

  public static final Logger log = LoggerFactory.getLogger(AuthIntegrationTest.class);

  private static final String KEY_SET_FILENAME = "keyset.json";
  private static final String ISSUER = "http://identity.example.com";
  private static final String DIR = "authtest";
  private static final String USERNAME = "user";
  private static final String PASSWORD = "password";

  private String oAuthToken;
  private String basicAuthToken;
  private File dir;
  public DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  @BeforeClass
  public void beforeClass() throws Exception {
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    oauthSetup();
    basicAuthSetup();

    SUPPORT = new DropwizardTestSupport<>(ThirdEyeServer.class,
        resourceFilePath("auth/server.yaml"),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", dbConfiguration.getUrl()),
        config("database.user", dbConfiguration.getUser()),
        config("database.password", dbConfiguration.getPassword()),
        config("database.driver", dbConfiguration.getDriver()),
        config("auth.enabled", "true"),
        config("auth.oauth.keysUrl",
            String.format("file://%s/%s", dir.getAbsolutePath(), KEY_SET_FILENAME)),
        config("auth.basic.enabled", "true"),
        config("auth.basic.users[0].username", USERNAME),
        config("auth.basic.users[0].password", PASSWORD)
    );
    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test client");
  }

  private void basicAuthSetup() {
    final String token = Base64.getEncoder().encodeToString(String.format("%s:%s", USERNAME, PASSWORD).getBytes());
    basicAuthToken = String.format("Basic %s", token);
  }

  private void oauthSetup() throws Exception {
    final JWKSet jwks = getJWKS(RandomStringUtils.randomAlphanumeric(16));
    final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test")
        .issuer(ISSUER)
        .expirationTime(new Date(System.currentTimeMillis() + 36000000))
        .build();
    oAuthToken = String.format("Bearer %s", getToken(jwks.getKeys().get(0), claimsSet));

    dir = new File(DIR);
    dir.mkdir();
    final FileWriter jwkFileWriter = new FileWriter(String.format("%s/%s", DIR, KEY_SET_FILENAME));
    jwkFileWriter.write(jwks.toString());
    jwkFileWriter.close();
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() throws Exception {
    log.info("ThirdEye port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();

    optional(dir.listFiles())
        .map(Arrays::stream)
        .ifPresent(files -> files.forEach(File::delete));
    dir.delete();
    MySqlTestDatabase.cleanSharedDatabase();
  }


  @Test
  public void testOAuthAuthorisedPingRequest() {
    final Response response = client.target(thirdEyeAuthEndPoint())
        .request()
        .header(HttpHeaders.AUTHORIZATION, oAuthToken)
        .post(null);
    assertThat(response.getStatus()).isEqualTo(200);

  }

  @Test
  public void testBasicAuthorisedPingRequest() {
    final Response response = client.target(thirdEyeAuthEndPoint())
        .request()
        .header(HttpHeaders.AUTHORIZATION, basicAuthToken)
        .post(null);
    assertThat(response.getStatus()).isEqualTo(200);

  }

  @Test
  public void testUnauthorisedPingRequest() {
    final Response response = client.target(thirdEyeAuthEndPoint())
        .request()
        .post(null);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  private String thirdEyeAuthEndPoint() {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), "api/auth/login");
  }
}


