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
package ai.startree.thirdeye.spi;

import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.auth.basic.BasicCredentials;
import java.security.Principal;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyePrincipal implements Principal {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyePrincipal.class);
  public static final String NAME_CLAIM = "email";

  private String name;

  public ThirdEyePrincipal(final String name) {
    this.name = name;
  }

  public ThirdEyePrincipal(final JWTClaimsSet claims) {
    try {
      this.name = claims.getStringClaim(NAME_CLAIM);
    } catch (ParseException e) {
      log.error("Could not get user name. email should be a String", e);
      this.name = null;
    }
  }

  public ThirdEyePrincipal(final BasicCredentials credentials) {
    this.name = credentials.getUsername();
  }

  @Override
  public String getName() {
    return name;
  }
}
