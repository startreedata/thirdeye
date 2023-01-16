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

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.spi.api.UiConfigurationApi;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "UI Configuration")
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
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return respondOk(
            new UiConfigurationApi()
                .setClientId(configuration.getClientId())
                .setAuthEnabled(authConfiguration.isEnabled())
        );
    }
}
