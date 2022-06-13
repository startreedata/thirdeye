/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import static ai.startree.thirdeye.utils.AuthTestUtils.getJWKS;
import static ai.startree.thirdeye.utils.AuthTestUtils.getToken;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.database.ThirdEyeH2DatabaseServer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OAuthIntegrationTest {

  public static final Logger log = LoggerFactory.getLogger(OAuthIntegrationTest.class);
  public static final String THIRDEYE_CONFIG = "./src/test/resources/auth";

  private static final String KEY_SET_FILENAME = "keyset.json";
  private static final String ISSUER = "http://identity.example.com";
  private static final String DIR = "authtest";

  private String token;
  private File dir;
  public DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;
  private ThirdEyeH2DatabaseServer db;

  @BeforeClass
  public void beforeClass() throws Exception {
    db = new ThirdEyeH2DatabaseServer("localhost", 7123, "OAuthIntegrationTest");
    db.start();
    db.truncateAllTables();

    oauthSetup();

    SUPPORT = new DropwizardTestSupport<>(ThirdEyeServer.class,
        resourceFilePath("auth/server.yaml"),
        config("configPath", THIRDEYE_CONFIG),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", db.getDbConfig().getUrl()),
        config("database.user", db.getDbConfig().getUser()),
        config("database.password", db.getDbConfig().getPassword()),
        config("database.driver", db.getDbConfig().getDriver()),
        config("auth.enabled", "true"),
        config("auth.oauth.keysUrl",
            String.format("file://%s/%s", dir.getAbsolutePath(), KEY_SET_FILENAME))
    );
    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test client");
  }

  private void oauthSetup() throws Exception {
    JWKSet jwks = getJWKS(RandomStringUtils.randomAlphanumeric(16));
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("test")
        .issuer(ISSUER)
        .expirationTime(new Date(System.currentTimeMillis() + 36000000))
        .build();
    token = String.format("Bearer %s", getToken(jwks.getKeys().get(0), claimsSet));

    dir = new File(DIR);
    dir.mkdir();
    FileWriter jwkFileWriter = new FileWriter(String.format("%s/%s", DIR, KEY_SET_FILENAME));
    jwkFileWriter.write(jwks.toString());
    jwkFileWriter.close();
  }

  @AfterClass
  public void afterClass() throws Exception {
    log.info("Thirdeye port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    db.stop();

    Optional.ofNullable(dir.listFiles()).map(Arrays::stream).ifPresent(files -> files.forEach(File::delete));
    dir.delete();
  }


  @Test
  public void testAuthorisedPingRequest() {
    Response response = client.target(thirdEyeEndPoint("api/auth/login"))
        .request()
        .header(HttpHeaders.AUTHORIZATION, token)
        .post(null);
    assertThat(response.getStatus()).isEqualTo(200);

  }

  @Test
  public void testUnauthorisedPingRequest() {
    Response response = client.target(thirdEyeEndPoint("api/auth/login"))
        .request()
        .post(null);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  private String thirdEyeEndPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }
}


