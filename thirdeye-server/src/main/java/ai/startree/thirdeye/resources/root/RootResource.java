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
package ai.startree.thirdeye.resources.root;

import ai.startree.thirdeye.resources.ApiResource;
import ai.startree.thirdeye.resources.InternalResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Note: Root resource is in a different package to make swagger parsing work.
 * See swagger in server.yaml.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "zzz All Endpoints zzz")
public class RootResource {

  private final ApiResource apiResource;
  private final InternalResource internalResource;

  @Inject
  public RootResource(final ApiResource apiResource,
      final InternalResource internalResource) {
    this.apiResource = apiResource;
    this.internalResource = internalResource;
  }

  @GET
  public Response home() {
    return Response
        .ok("ThirdEye Coordinator is up and running.")
        .build();
  }

  // for some instances the coordinator can be exposed on the public web
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("robots.txt")
  public Response robots() {
    return Response.ok("User-agent: *\nDisallow: /\n").header("Content-Type", "text/plain").build();
  }

  @GET
  @Produces("image/x-icon")
  @Path("favicon.ico")
  public byte[] favicon() {
    // no favicon for the moment
    return new byte[0];
  }

  @Path("api")
  public ApiResource getApiResource() {
    return apiResource;
  }

  @Path("internal")
  public InternalResource getInternalResource() {
    return internalResource;
  }
}
