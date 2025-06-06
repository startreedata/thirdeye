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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.TaskService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Tag(name = "Task")
@SecurityRequirement(name = "oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource extends CrudResource<TaskApi, TaskDTO> {

  public static final String N_DAYS_TO_DELETE = "30";
  public static final String MAX_ENTRIES_TO_DELETE = "1000";

  private final TaskService taskService;

  @Inject
  public TaskResource(final TaskService taskService) {
    super(taskService);
    this.taskService = taskService;
  }

  // Overridden to disable endpoint
  @Override
  @POST
  @Operation(summary = "", hidden = true)
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  @Override
  @PUT
  @Operation(summary = "", hidden = true)
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  @DELETE
  @Path("/purge")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response purge(@Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @Parameter(description = "Older than (number of days)", example = N_DAYS_TO_DELETE) @QueryParam("olderThanInDays") Integer nDays,
      @Parameter(description = "Max Entries to delete", example = MAX_ENTRIES_TO_DELETE) @QueryParam("limit") Integer limitOptional
  ) {
    final int nDaysToDelete = optional(nDays).orElse(Integer.valueOf(N_DAYS_TO_DELETE));
    final int limit = optional(limitOptional).orElse(Integer.valueOf(MAX_ENTRIES_TO_DELETE));

    taskService.purge(principal, nDaysToDelete, limit);
    return Response.ok().build();
  }
}
