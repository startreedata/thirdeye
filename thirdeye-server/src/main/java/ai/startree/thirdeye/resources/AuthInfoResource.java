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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.oauth.OAuthManager;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Auth Info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AuthInfoResource {

  private final OAuthManager oAuthManager;
  private final AuthConfiguration authConfig;

  @Inject
  public AuthInfoResource(final OAuthManager oAuthManager, final AuthConfiguration authConfig) {
    this.oAuthManager = oAuthManager;
    this.authConfig = authConfig;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    final AuthInfoApi info = oAuthManager.getInfo();
    if (authConfig.isEnabled() && info == null) {
      throw serverError(ThirdEyeStatus.ERR_AUTH_SERVER_NOT_RESPONDING, authConfig.getOAuthConfig().getServerUrl());
    }
    return respondOk(info);
  }
}
