package ai.startree.thirdeye.spi.authorization;

import java.net.http.HttpHeaders;

public interface AccessControl {

  boolean hasAccess(
      String name,
      String namespace,
      EntityType entityType,
      AccessType accessType,
      HttpHeaders httpHeaders
  );
}
