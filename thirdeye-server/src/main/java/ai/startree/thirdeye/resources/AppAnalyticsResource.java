package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.spi.api.AppAnalyticsApi;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "App Analytics")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AppAnalyticsResource {

  @Inject
  public AppAnalyticsResource() {
  }

  public static String appVersion() {
    return AppAnalyticsResource.class.getPackage().getImplementationVersion();
  }

  @GET
  public Response getAnalyticsPayload() {
    return Response.ok(new AppAnalyticsApi()
        .setVersion(appVersion())
    ).build();
  }

  @GET
  @Path("version")
  public Response getVersion() {
    return Response.ok(appVersion()).build();
  }
}
