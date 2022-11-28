package ai.startree.thirdeye.spi.authorization;

import java.net.http.HttpHeaders;

public class AlwaysAllow implements AccessController {

  @Override
  public boolean hasAccess(
      String name,
      String namespace,
      EntityType entityType,
      AccessType accessType,
      HttpHeaders httpHeaders
  ) {
    return true;
  }
}
