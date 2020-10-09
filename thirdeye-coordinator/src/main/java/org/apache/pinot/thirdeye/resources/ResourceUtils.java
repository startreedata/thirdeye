package org.apache.pinot.thirdeye.resources;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;

public class ResourceUtils {

  public static void authenticate(boolean condition) {
    if (!condition) {
      throw new NotAuthorizedException("Authentication Failure."); // throw 401
    }
  }

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
