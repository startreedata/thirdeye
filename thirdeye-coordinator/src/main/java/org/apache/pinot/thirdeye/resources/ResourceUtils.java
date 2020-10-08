package org.apache.pinot.thirdeye.resources;

import javax.ws.rs.BadRequestException;

public class ResourceUtils {

  public static void ensure(boolean condition, String message) {
    if (!condition) {
      throw new BadRequestException(message);
    }
  }

  public static <T> T ensureExists(T o, String message) {
    ensure(o != null, message);
    return o;
  }
}
