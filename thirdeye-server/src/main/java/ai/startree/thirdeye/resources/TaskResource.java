/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
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
import javax.ws.rs.FormParam;
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
      .put("startTime", "startTime")
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

  @POST
  @Path("/handle-orphan")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response handleOrphan(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @FormParam("startedBeforeSeconds") final Long olderThanSeconds) {
    final MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    final long olderThan = System.currentTimeMillis() - olderThanSeconds*1000;
    map.add("startTime", "[lt]" + olderThan);
    map.add("status", TaskStatus.RUNNING.toString());

    dtoManager
        .filter(new DaoFilterBuilder(apiToBeanMap).buildFilter(map))
        .forEach(task -> ((TaskManager)dtoManager).updateStatusAndTaskEndTime(task.getId(),
            TaskStatus.RUNNING,
            TaskStatus.FAILED,
            System.currentTimeMillis(),
            "Orphan task handled by updating status to FAILED"));
    return Response.ok().build();
  }
}
