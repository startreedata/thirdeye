/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.time.Duration;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Task", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource extends CrudResource<TaskApi, TaskDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("type", "type")
      .put("status", "status")
      .put("created", "createTime")
      .put("updated", "updateTime")
      .build();
  public static final String N_DAYS_TO_DELETE = "30";
  public static final String MAX_ENTRIES_TO_DELETE = "1000";

  private final TaskManager taskManager;

  @Inject
  public TaskResource(final TaskManager taskManager) {
    super(taskManager, API_TO_INDEX_FILTER_MAP);
    this.taskManager = taskManager;
  }

  // Operation not supported to prevent create of tasks
  @Override
  protected TaskDTO createDto(final ThirdEyePrincipal principal, final TaskApi taskApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected TaskApi toApi(final TaskDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  // Overridden to disable endpoint
  @Override
  @POST
  @ApiOperation(value = "", hidden = true)
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  @Override
  @PUT
  @ApiOperation(value = "", hidden = true)
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  @DELETE
  @Path("/purge")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response purge(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(value = "Older than (number of days)", defaultValue = N_DAYS_TO_DELETE) @QueryParam("olderThanInDays") Integer nDays,
      @ApiParam(value = "Max Entries to delete", defaultValue = MAX_ENTRIES_TO_DELETE) @QueryParam("limit") Integer limitOptional
  ) {
    final int nDaysToDelete = optional(nDays).orElse(Integer.valueOf(N_DAYS_TO_DELETE));
    final int limit = optional(limitOptional).orElse(Integer.valueOf(MAX_ENTRIES_TO_DELETE));

    taskManager.purge(Duration.ofDays(nDaysToDelete), limit);
    return Response.ok().build();
  }
}
