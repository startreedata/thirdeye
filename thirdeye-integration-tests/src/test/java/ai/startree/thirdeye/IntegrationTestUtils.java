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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import java.io.File;

public class IntegrationTestUtils {

  public static JWKSet getJWKS(String kid) throws JOSEException {
    return new JWKSet(new RSAKeyGenerator(2048)
        .keyID(kid)
        .generate());
  }

  public static String getToken(JWK key, JWTClaimsSet claims) throws JOSEException {
    JWSObject jwsObject = new JWSObject(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
        claims.toPayload());
    jwsObject.sign(new RSASSASigner((RSAKey) key));
    return jwsObject.serialize();
  }

  public static String cleanSql(String sql) {
    return sql
        .trim()
        .replaceAll("[\\n\\t\\r]+", " ")
        .replaceAll(" +", " ");
  }

  public static void setupPluginsDirAbsolutePath() {
    final String projectBuildDirectory = requireNonNull(System.getProperty("projectBuildDirectory"),
        "project build dir not set");
    final String projectVersion = requireNonNull(System.getProperty("projectVersion"),
        "project version not set");
    final String pluginsPath = new StringBuilder()
        .append(projectBuildDirectory)
        .append("/../../thirdeye-distribution/target/thirdeye-distribution-")
        .append(projectVersion)
        .append("-dist/thirdeye-distribution-")
        .append(projectVersion)
        .append("/plugins")
        .toString();
    final File pluginsDir = new File(pluginsPath);
    assertThat(pluginsDir.exists()).isTrue();
    assertThat(pluginsDir.isDirectory()).isTrue();

    System.setProperty(SYS_PROP_THIRDEYE_PLUGINS_DIR, pluginsDir.getAbsolutePath());
  }
}
