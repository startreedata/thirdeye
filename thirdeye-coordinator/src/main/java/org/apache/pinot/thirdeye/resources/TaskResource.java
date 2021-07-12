package org.apache.pinot.thirdeye.resources;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.TaskApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;

@Api(tags = "Task")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource extends CrudResource<TaskApi, TaskDTO> {

  private final TaskManager taskManager;
  private final AuthService authService;

  @Inject
  public TaskResource(final AuthService authService, final TaskManager taskManager) {
    super(authService, taskManager, ImmutableMap.of());
    this.taskManager = taskManager;
    this.authService = authService;
  }

  @Override
  protected TaskDTO createDto(final ThirdEyePrincipal principal, final TaskApi taskApi) {
    final TaskDTO dto = toDto(taskApi);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected TaskDTO toDto(final TaskApi api) {
    return ApiBeanMapper.toTaskDto(api);
  }

  @Override
  protected TaskApi toApi(final TaskDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  // Overridden to disable endpoint
  @Override
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  @Override
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  // TODO: expose once id is in place
  @Override
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  // TODO: expose once id is in place
  @Override
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    throw new UnsupportedOperationException();
  }

}
