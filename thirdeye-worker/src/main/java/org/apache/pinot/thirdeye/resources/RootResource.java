package org.apache.pinot.thirdeye.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz All Endpoints zzz")
@Singleton
public class RootResource {

  private final InternalResource internalResource;

  @Inject
  public RootResource(
      final InternalResource internalResource) {
    this.internalResource = internalResource;
  }

  @GET
  public Response home() {
    return Response.ok("ThirdEye Worker is up and running.").build();
  }

  @Path("internal")
  public InternalResource getInternalResource() {
    return internalResource;
  }
}
