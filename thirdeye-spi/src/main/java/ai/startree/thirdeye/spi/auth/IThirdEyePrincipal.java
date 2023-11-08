package ai.startree.thirdeye.spi.auth;

import java.security.Principal;

public interface IThirdEyePrincipal extends Principal {

  @Override
  String getName();

  String getAuthToken();

  AuthenticationType getAuthenticationType();
}
