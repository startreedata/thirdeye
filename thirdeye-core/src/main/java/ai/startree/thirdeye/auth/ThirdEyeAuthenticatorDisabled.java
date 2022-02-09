package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import com.nimbusds.jwt.JWTClaimsSet;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;

public class ThirdEyeAuthenticatorDisabled implements Authenticator<String, ThirdEyePrincipal> {

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final String s) throws AuthenticationException {
    return Optional.of(new ThirdEyePrincipal(new JWTClaimsSet.Builder().subject(s)
        .build()));
  }
}
