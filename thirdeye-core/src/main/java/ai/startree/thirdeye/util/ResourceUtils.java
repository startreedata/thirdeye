/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.util;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_UNEXPECTED;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.api.StatusListApi;
import ai.startree.thirdeye.spi.api.ThirdEyeApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResourceUtils {

  private final static ObjectMapper J = new ObjectMapper();

  public static Response respondOk(final ThirdEyeApi api) {
    return Response.ok(api).build();
  }

  public static <T extends ThirdEyeApi> Response respondOk(final Collection<T> api) {
    return Response.ok(api).build();
  }

  public static <T extends ThirdEyeApi> Response respondOk(final Stream<T> api) {
    return Response.ok(api).build();
  }

  @SuppressWarnings("unused")
  public static void authenticate(final boolean condition) {
    if (!condition) {
      throw unauthenticatedException();
    }
  }

  @SuppressWarnings("unused")
  public static void authorize(final boolean condition) {
    authorize(condition, "Access Denied.");
  }

  @SuppressWarnings("unused")
  public static void authorize(final boolean condition, final String errorMessage) {
    if (!condition) {
      throw new ForbiddenException(errorMessage);  // throw 403
    }
  }

  public static NotAuthorizedException unauthenticatedException() {
    return new NotAuthorizedException("Authentication Failure.");  // throw 401
  }

  public static <T> T ensureExists(final T o) {
    return ensureExists(o, ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST, "");
  }

  public static <T> T ensureExists(final T o, final Object... args) {
    return ensureExists(o, ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST, args);
  }

  public static <T> T ensureExists(final T o, final ThirdEyeStatus status, final Object... args) {
    ensure(o != null, status, args);
    return o;
  }

  public static void ensureNull(final Object o, final String message) {
    ensure(o == null, ERR_OBJECT_UNEXPECTED, message);
  }

  public static void ensureNull(final Object o, final ThirdEyeStatus status, final Object... args) {
    ensure(o == null, status, args);
  }

  public static void ensure(final boolean condition, final String message) {
    ensure(condition, ThirdEyeStatus.ERR_UNKNOWN, message);
  }

  public static void ensure(final boolean condition, final ThirdEyeStatus status,
      final Object... args) {
    if (!condition) {
      throw badRequest(status, args);
    }
  }

  public static StatusListApi statusResponse(final ThirdEyeException e) {
    return statusListApi(e.getStatus(), e.getMessage());
  }

  public static StatusListApi statusResponse(final ThirdEyeStatus status, final Object... args) {
    return statusListApi(status, String.format(status.getMessage(), args));
  }

  public static StatusListApi statusListApi(final ThirdEyeStatus status, final String msg) {
    final StatusApi statusApi = new StatusApi()
        .setCode(status)
        .setMsg(msg);
    return statusListApi(statusApi);
  }

  public static StatusListApi statusListApi(final StatusApi statusApi) {
    return new StatusListApi().setList(ImmutableList.of(statusApi));
  }

  public static StatusApi statusApi(final ThirdEyeStatus status, final Object... args) {
    return new StatusApi()
        .setCode(status)
        .setMsg(String.format(status.getMessage(), args));
  }

  public static BadRequestException badRequest(final ThirdEyeStatus status, final Object... args) {
    return badRequest(statusResponse(status, args));
  }

  public static BadRequestException badRequest(final StatusListApi response) {
    return new BadRequestException(Response
        .status(Status.BAD_REQUEST)
        .entity(response)
        .build()
    );
  }

  public static InternalServerErrorException serverError(final ThirdEyeStatus status,
      final Object... args) {
    return serverError(statusResponse(status, args));
  }

  public static InternalServerErrorException serverError(final StatusListApi response) {
    return new InternalServerErrorException(Response
        .status(Status.INTERNAL_SERVER_ERROR)
        .entity(response)
        .build()
    );
  }

  public static List<Map<String, Object>> resultSetToMap(final @Nullable ResultSet rs) throws SQLException {
    if (rs == null) {
      return null;
    }
    final List<Map<String, Object>> list = new ArrayList<>();
    final ResultSetMetaData rsmd = rs.getMetaData();
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
      } catch (final JsonProcessingException e) {
        return object;
      }
    }
    return object;
  }
}
