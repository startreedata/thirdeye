package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.spi.Constants.AUTH_BEARER;
import static ai.startree.thirdeye.spi.Constants.NO_AUTH_USER;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;

@Priority(Priorities.AUTHENTICATION-1)
public class AuthDisabledRequestFilter implements ContainerRequestFilter {

  @Override
  public void filter(final ContainerRequestContext context) throws IOException {
    context.getHeaders()
        .addFirst(HttpHeaders.AUTHORIZATION, String.format("%s %s", AUTH_BEARER, NO_AUTH_USER));
  }
}
