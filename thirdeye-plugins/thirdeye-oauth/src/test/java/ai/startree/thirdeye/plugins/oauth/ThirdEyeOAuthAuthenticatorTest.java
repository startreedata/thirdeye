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

package ai.startree.thirdeye.plugins.oauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

import com.google.common.cache.CacheLoader;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThirdEyeOAuthAuthenticatorTest {

  private CacheLoader<String, ThirdEyePrincipal> cache;

  public static JWK getJWK(String kid) throws JOSEException {
    return new RSAKeyGenerator(2048)
        .keyID(kid)
        .generate();
  }

  public static String getToken(JWK key, JWTClaimsSet claims) throws JOSEException {
    JWSObject jwsObject = new JWSObject(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
        claims.toPayload());
    jwsObject.sign(new RSASSASigner((RSAKey) key));
    return jwsObject.serialize();
  }

  @BeforeClass
  public void init() throws Exception {
    final OidcJWTProcessor processor = mock(OidcJWTProcessor.class);
    when(processor.process(any(SignedJWT.class), any(OidcContext.class)))
        .thenReturn(new JWTClaimsSet.Builder().claim("email", "test")
        .build());

    final ThirdEyeOAuthAuthenticator authenticator = new ThirdEyeOAuthAuthenticator(
        processor,
        mock(OidcContext.class),
        new OAuthConfiguration()
    );
    cache = authenticator.getCacheLoader();
  }

  @Test
  public void cachedEntriesTest() throws Exception {
    ThirdEyePrincipal principal = cache.load(getToken(getJWK(
            RandomStringUtils.randomAlphanumeric(16)),
        new JWTClaimsSet.Builder().build()));
    assertNotNull(principal);
    assertEquals(principal.getName(), "test");
  }

  @Test
  public void invalidJwtTokenTest() {
    expectThrows(Exception.class, () -> cache.load("invalid-token"));
  }
}
