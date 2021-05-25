package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_UNEXPECTED;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.api.StatusApi;
import org.apache.pinot.thirdeye.spi.api.StatusListApi;
import org.apache.pinot.thirdeye.spi.api.ThirdEyeApi;

public class ResourceUtils {

  public static Response respondOk(ThirdEyeApi api) {
    return Response.ok(api).build();
  }

  public static <T extends ThirdEyeApi> Response respondOk(Collection<T> api) {
    return Response.ok(api).build();
  }

  public static <T extends ThirdEyeApi> Response respondOk(Stream<T> api) {
    return Response.ok(api).build();
  }

  public static void authenticate(boolean condition) {
    if (!condition) {
      throw unauthenticatedException();
    }
  }

  public static NotAuthorizedException unauthenticatedException() {
    return new NotAuthorizedException("Authentication Failure.");  // throw 401
  }

  public static <T> T ensureExists(T o) {
    return ensureExists(o, ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST, "");
  }

  public static <T> T ensureExists(T o, Object... args) {
    return ensureExists(o, ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST, args);
  }

  public static <T> T ensureExists(T o, ThirdEyeStatus status, Object... args) {
    ensure(o != null, status, args);
    return o;
  }

  public static void ensureNull(Object o, String message) {
    ensure(o == null, ERR_OBJECT_UNEXPECTED, message);
  }

  public static void ensureNull(Object o, ThirdEyeStatus status, Object... args) {
    ensure(o == null, status, args);
  }

  public static void ensure(boolean condition, String message) {
    ensure(condition, ThirdEyeStatus.ERR_UNKNOWN, message);
  }

  public static void ensure(boolean condition, ThirdEyeStatus status, Object... args) {
    if (!condition) {
      throw badRequest(status, args);
    }
  }

  public static StatusListApi statusResponse(ThirdEyeStatus status, Object... args) {
    return statusListApi(status, String.format(status.getMessage(), args));
  }

  public static StatusListApi statusListApi(ThirdEyeStatus status, String msg) {
    return new StatusListApi()
        .setList(ImmutableList.of(new StatusApi()
            .setCode(status)
            .setMsg(msg)
        ));
  }

  public static BadRequestException badRequest(ThirdEyeStatus status, Object... args) {
    return badRequest(statusResponse(status, args));
  }

  public static BadRequestException badRequest(final StatusListApi response) {
    return new BadRequestException(Response
        .status(Status.BAD_REQUEST)
        .entity(response)
        .build()
    );
  }

  public static InternalServerErrorException serverError(ThirdEyeStatus status, Object... args) {
    return serverError(statusResponse(status, args));
  }

  public static InternalServerErrorException serverError(final StatusListApi response) {
    return new InternalServerErrorException(Response
        .status(Status.INTERNAL_SERVER_ERROR)
        .entity(response)
        .build()
    );
  }

  /**
   * Return a list of parameters.
   * Support both multi-entity notations:
   * <br/><b>(1) comma-delimited:</b> {@code "urns=thirdeye:metric:123,thirdeye:metric:124"}
   * <br/><b>(2) multi-param</b> {@code "urns=thirdeye:metric:123&urns=thirdeye:metric:124"}
   *
   * @param params input of params
   * @return list of params
   */
  public static List<String> parseListParams(List<String> params) {
    if (params == null){
      return Collections.emptyList();
    }
    if (params.size() != 1)
      return params;
    return Arrays.asList(params.get(0).split(","));
  }

}
