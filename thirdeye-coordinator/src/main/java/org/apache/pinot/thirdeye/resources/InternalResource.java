package org.apache.pinot.thirdeye.resources;

import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.datasource.pinot.resources.PinotDataSourceResource;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz Internal zzz")
public class InternalResource {

  private final PinotDataSourceResource pinotDataSourceResource;

  @Inject
  public InternalResource(final PinotDataSourceResource pinotDataSourceResource) {
    this.pinotDataSourceResource = pinotDataSourceResource;
  }

  @Path("pinot-data-source")
  public PinotDataSourceResource getPinotDataSourceResource() {
    return pinotDataSourceResource;
  }
}
