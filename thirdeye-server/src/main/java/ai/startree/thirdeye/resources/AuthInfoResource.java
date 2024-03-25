/*
 * Copyright 2024 StarTree Inc
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
import ai.startree.thirdeye.service.AuthService;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "Auth Info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AuthInfoResource {

  private final AuthService authService;
  private final AuthConfiguration authConfig;

  @Inject
  public AuthInfoResource(final AuthService authService, final AuthConfiguration authConfig) {
    this.authService = authService;
    this.authConfig = authConfig;
  }

  @GET
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    final AuthInfoApi info = authService.getOpenIdConfiguration(null);
    if (authConfig.isEnabled() && info == null) {
      throw serverError(ThirdEyeStatus.ERR_AUTH_SERVER_NOT_RESPONDING,
          authConfig.getOAuthConfig().getServerUrl());
    }
    return respondOk(info);
  }
}
