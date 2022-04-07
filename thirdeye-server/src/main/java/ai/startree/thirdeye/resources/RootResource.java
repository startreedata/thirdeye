/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
