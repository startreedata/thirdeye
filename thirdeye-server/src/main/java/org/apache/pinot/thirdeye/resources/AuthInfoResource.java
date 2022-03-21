package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.auth.OidcUtils.getAuthInfo;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;

@Api(tags = "Auth Info")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AuthInfoResource {
  private String infoURL;

  @Inject
  public AuthInfoResource(AuthConfiguration authConfig){
    Optional.ofNullable(authConfig.getInfoURL()).ifPresent(url -> this.infoURL = url.trim());
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    if(infoURL == null || infoURL.isEmpty()){
      return Response.ok().build();
    }
    return Response.ok(getAuthInfo(infoURL)).build();
  }
}
