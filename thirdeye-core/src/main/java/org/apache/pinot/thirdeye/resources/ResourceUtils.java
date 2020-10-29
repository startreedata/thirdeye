package org.apache.pinot.thirdeye.resources;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.pinot.thirdeye.ThirdEyeStatus;
import org.apache.pinot.thirdeye.api.StatusApi;
import org.apache.pinot.thirdeye.api.StatusListApi;

public class ResourceUtils {

  public static void authenticate(boolean condition) {
    if (!condition) {
      throw unauthenticatedException();
    }
  }

  public static NotAuthorizedException unauthenticatedException() {
    return new NotAuthorizedException("Authentication Failure.");  // throw 401
  }

  public static void ensure(boolean condition, String message) {
    if (!condition) {
      throw new BadRequestException(message);
    }
  }

  public static void ensure(boolean condition, ThirdEyeStatus status) {
    if (!condition) {
      throw badRequest(status);
    }
  }

  public static <T> T ensureExists(T o, String message) {
    ensure(o != null, message);
    return o;
  }

  public static void ensureNull(Object o, String message) {
    ensure(o == null, message);
  }

  public static StatusListApi statusResponse(ThirdEyeStatus status) {
    return new StatusListApi().setList(ImmutableList.of(new StatusApi(status)));
  }

  public static BadRequestException badRequest(ThirdEyeStatus status) {
    return new BadRequestException(Response
        .status(Status.BAD_REQUEST)
        .entity(statusResponse(status))
        .build()
    );
  }
}
