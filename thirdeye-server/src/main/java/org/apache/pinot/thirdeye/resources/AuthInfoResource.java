package org.apache.pinot.thirdeye.resources;


import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.OAuthManager;

@Api(tags = "Auth Info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AuthInfoResource {
  private final OAuthManager oAuthManager;

  @Inject
  public AuthInfoResource(OAuthManager oAuthManager){
    this.oAuthManager = oAuthManager;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    return Response.ok(oAuthManager.getInfo()).build();
  }
}
