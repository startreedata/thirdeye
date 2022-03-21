package org.apache.pinot.thirdeye.util;

import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_UNEXPECTED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private final static ObjectMapper J = new ObjectMapper();

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

  public static StatusApi statusApi(final ThirdEyeStatus status, final Object... args) {
    return new StatusApi()
        .setCode(status)
        .setMsg(String.format(status.getMessage(), args));
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

  public static List<Map<String, Object>> resultSetToMap(final ResultSet rs) throws SQLException {
    List<Map<String, Object>> list = new ArrayList<>();
    ResultSetMetaData rsmd = rs.getMetaData();
    while (rs.next()) {
      final Map<String, Object> map = new HashMap<>();
      for (int i = 1; i <= rsmd.getColumnCount(); i++) {
        final String columnName = rsmd.getColumnName(i);
        map.put(columnName, handleObject(rs.getObject(columnName)));
      }
      list.add(map);
    }
    return list;
  }

  private static Object handleObject(final Object object) {
    if (object instanceof String) {
      try {
        return J.readValue(object.toString(), Map.class);
      } catch (JsonProcessingException e) {
        return object;
      }
    }
    return object;
  }
}
