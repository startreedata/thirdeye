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

import static ai.startree.thirdeye.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.mapper.UiConfigurationMapper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "UI Configuration")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class UiResource {

  private final UiConfiguration configuration;
  private final AuthConfiguration authConfiguration;

  @Inject
  public UiResource(final UiConfiguration uiConfiguration,
      final AuthConfiguration authConfiguration) {
    this.configuration = uiConfiguration;
    this.authConfiguration = authConfiguration;
  }

  @GET
  @Path("config")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    return respondOk(UiConfigurationMapper.INSTANCE.toApi(configuration)
        .setAuthEnabled(authConfiguration.isEnabled())
    );
  }
}
