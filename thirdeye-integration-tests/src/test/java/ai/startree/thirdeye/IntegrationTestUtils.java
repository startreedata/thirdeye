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

import static ai.startree.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static ai.startree.thirdeye.spi.datalayer.TemplatableMap.fromValueMap;
import static com.google.api.client.util.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
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
import java.util.Map;

public class IntegrationTestUtils {

  public static final String ENUMERATION_ITEMS_KEY = "enumerationItems";
  static final String NODE_NAME_ROOT = "root";
  static final String NODE_NAME_CHILD_ROOT = "childRoot";
  static final String NODE_NAME_ENUMERATOR = "enumerator";
  static final String NODE_NAME_COMBINER = "combiner";
  static final String TYPE_ENUMERATOR = "Enumerator";
  static final String TYPE_FORK_JOIN = "ForkJoin";
  static final String TYPE_COMBINER = "Combiner";

  static PlanNodeApi forkJoinNode() {
    return new PlanNodeApi()
        .setName(NODE_NAME_ROOT)
        .setType(TYPE_FORK_JOIN)
        .setParams(fromValueMap(Map.of(
            "enumerator", NODE_NAME_ENUMERATOR,
            "combiner", NODE_NAME_COMBINER,
            "root", NODE_NAME_CHILD_ROOT
        )));
  }

  static PlanNodeApi enumeratorNode() {
    return new PlanNodeApi()
        .setName(NODE_NAME_ENUMERATOR)
        .setType(TYPE_ENUMERATOR)
        .setParams(fromValueMap(Map.of("items", "${" + ENUMERATION_ITEMS_KEY + "}")));
  }

  static PlanNodeApi combinerNode() {
    return new PlanNodeApi()
        .setName(NODE_NAME_COMBINER)
        .setType(TYPE_COMBINER);
  }

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
    checkState(pluginsDir.exists() && pluginsDir.isDirectory(),
        "Could not find plugins directory %s. Try to rebuild TE?", pluginsPath);

    System.setProperty(SYS_PROP_THIRDEYE_PLUGINS_DIR, pluginsDir.getAbsolutePath());
  }


  public static void assertAnomalyEquals(final AnomalyApi actual, final AnomalyApi expected) {
    assertThat(actual.getStartTime()).isEqualTo(expected.getStartTime());
    assertThat(actual.getEndTime()).isEqualTo(expected.getEndTime());
    assertThat(actual.getAvgBaselineVal()).isEqualTo(expected.getAvgBaselineVal());
    assertThat(actual.getAvgCurrentVal()).isEqualTo(expected.getAvgCurrentVal());
    if (expected.getAnomalyLabels() != null) {
      assertThat(actual.getAnomalyLabels()).hasSize(expected.getAnomalyLabels().size());
    }
  }
}
