package org.apache.pinot.thirdeye.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.TaskApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;

@Api(tags = "Task")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource extends CrudResource<TaskApi, TaskDTO> {

  public static final ImmutableMap<String, String> API_TO_BEAN_MAP = ImmutableMap.<String, String>builder()
      .put("type", "type")
      .put("status", "status")
      .build();

  @Inject
  public TaskResource(final TaskManager taskManager) {
    super(taskManager, API_TO_BEAN_MAP);
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
      @Auth ThirdEyePrincipal principal,
      List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  @Override
  @PUT
  @ApiOperation(value = "", hidden = true)
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @Auth ThirdEyePrincipal principal,
      List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }
}
