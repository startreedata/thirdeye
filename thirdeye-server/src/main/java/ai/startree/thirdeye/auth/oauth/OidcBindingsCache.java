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
package ai.startree.thirdeye.auth.oauth;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import com.google.common.cache.CacheLoader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcBindingsCache extends CacheLoader<String, ThirdEyePrincipal> {

  public static final String NAME_CLAIM = "email";
  private static final Logger log = LoggerFactory.getLogger(ThirdEyePrincipal.class);

  private OidcJWTProcessor processor;
  private OidcContext context;

  public static String getName(final JWTClaimsSet claims) {
    try {
      return claims.getStringClaim(NAME_CLAIM);
    } catch (ParseException e) {
      log.error("Could not get user name. email should be a String", e);
      return null;
    }
  }

  public OidcBindingsCache setProcessor(final OidcJWTProcessor processor) {
    this.processor = processor;
    return this;
  }

  public OidcBindingsCache setContext(final OidcContext context) {
    this.context = context;
    return this;
  }

  @Override
  public ThirdEyePrincipal load(@NotNull String authToken)
      throws Exception {
    SignedJWT jwt = SignedJWT.parse(authToken);
    JWTClaimsSet claims = processor.process(jwt, context);
    return new ThirdEyePrincipal(authToken, getName(claims));
  }
}
