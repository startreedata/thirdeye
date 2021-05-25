package org.apache.pinot.thirdeye.resources;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import io.swagger.annotations.Api;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "zzz Internal zzz")
public class InternalResource {

  private static final Package PACKAGE = InternalResource.class.getPackage();

  @GET
  @Path("ping")
  public Response ping() {
    return Response.ok("pong").build();
  }

  @GET
  @Path("version")
  public Response getVersion() {
    return Response.ok(InternalResource.class.getPackage().getImplementationVersion()).build();
  }

  @GET
  @Path("package-info")
  @JacksonFeatures(serializationEnable = {SerializationFeature.INDENT_OUTPUT})
  public Response getPackageInfo() {
    return Response.ok(PACKAGE).build();
  }
}
