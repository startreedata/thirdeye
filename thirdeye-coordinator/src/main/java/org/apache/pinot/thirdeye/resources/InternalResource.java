package org.apache.pinot.thirdeye.resources;

import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datasource.pinot.resources.PinotDataSourceResource;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "Internal")
public class InternalResource {

  private final PinotDataSourceResource pinotDataSourceResource;

  @Inject
  public InternalResource(final PinotDataSourceResource pinotDataSourceResource) {
    this.pinotDataSourceResource = pinotDataSourceResource;
  }

  @GET
  public Response home() {
    return Response
        .ok("ThirdEye Coordinator is up and running.")
        .build();
  }

  @Path("pinot-data-source")
  public PinotDataSourceResource getPinotDataSourceResource() {
    return pinotDataSourceResource;
  }
}
