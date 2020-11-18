package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_MISSING_ID;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.statusResponse;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.AbstractManager;
import org.apache.pinot.thirdeye.datalayer.dto.AbstractDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudResource<ApiType, DtoType extends AbstractDTO> {

  private static final Logger log = LoggerFactory.getLogger(CrudResource.class);

  protected final AuthService authService;
  protected final AbstractManager<DtoType> dtoManager;

  @Inject
  public CrudResource(
      final AuthService authService, final AbstractManager<DtoType> dtoManager) {
    this.dtoManager = dtoManager;
    this.authService = authService;
  }

  protected abstract DtoType createDto(final ThirdEyePrincipal principal, final ApiType api);

  protected abstract DtoType updateDto(final ThirdEyePrincipal principal, final ApiType api);

  protected abstract ApiType toApi(final DtoType dto);

  protected DtoType get(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader
  ) {
    authService.authenticate(authHeader);
    final List<DtoType> all = dtoManager.findAll();
    return Response
        .ok(all.stream().map(this::toApi))
        .build();
  }

  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApiType> list) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(list, "Invalid request");

    return Response
        .ok(list.stream()
            .map(alertApi -> createDto(principal, alertApi))
            .peek(dtoManager::save)
            .map(this::toApi)
            .collect(Collectors.toList())
        )
        .build();
  }

  @PUT
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApiType> list) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    return Response
        .ok(list.stream()
            .map(o -> updateDto(principal, o))
            .peek(dtoManager::update)
            .map(this::toApi)
            .collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    authService.authenticate(authHeader);
    final DtoType dto = dtoManager.findById(id);
    ensureExists(dto, "Invalid id");

    return Response
        .ok(toApi(dto))
        .build();
  }

  @DELETE
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final DtoType dto = dtoManager.findById(id);
    if (dto != null) {
      dtoManager.delete(dto);
      log.warn(String.format("Deleted id: %d by principal: %s", id, principal));

      return Response.ok(toApi(dto)).build();
    }

    return Response
        .ok(statusResponse(ERR_OBJECT_DOES_NOT_EXIST, id))
        .build();
  }
}
