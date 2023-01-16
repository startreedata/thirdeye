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

import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz All Endpoints zzz")
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

  @Path("api")
  public ApiResource getApiResource() {
    return apiResource;
  }

  @Path("internal")
  public InternalResource getInternalResource() {
    return internalResource;
  }
}
