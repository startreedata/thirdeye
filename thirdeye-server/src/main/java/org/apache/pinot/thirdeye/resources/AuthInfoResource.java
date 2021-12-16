package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.auth.OidcUtils.getAuthInfo;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import java.util.Collections;
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
  private AuthConfiguration authConfig;

  @Inject
  public AuthInfoResource(AuthConfiguration authConfig){
    this.authConfig = authConfig;
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    if(!authConfig.isEnabled() || authConfig.getInfoURL() == null || authConfig.getInfoURL().trim().isEmpty()){
      return Response.ok(Collections.EMPTY_MAP).build();
    }
    return Response.ok(getAuthInfo(authConfig.getInfoURL().trim())).build();
  }
}
