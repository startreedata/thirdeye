package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.respondOk;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.statusResponse;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_ID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Date;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.pinot.thirdeye.DaoFilterBuilder;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.spi.api.ThirdEyeCrudApi;
import org.apache.pinot.thirdeye.spi.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AbstractManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudResource<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger log = LoggerFactory.getLogger(CrudResource.class);

  protected final AuthService authService;
  protected final AbstractManager<DtoT> dtoManager;
  protected final ImmutableMap<String, String> apiToBeanMap;

  @Inject
  public CrudResource(
      final AuthService authService,
      final AbstractManager<DtoT> dtoManager,
      final ImmutableMap<String, String> apiToBeanMap) {
    this.dtoManager = dtoManager;
    this.authService = authService;
    this.apiToBeanMap = apiToBeanMap;
  }

  protected void validate(final ApiT api) {
    // override and perform necessary validations
  }

  protected abstract DtoT createDto(final ThirdEyePrincipal principal, final ApiT api);

  private DtoT updateDto(final ThirdEyePrincipal principal, final ApiT api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final DtoT existing = ensureExists(dtoManager.findById(id));

    final DtoT updated = toDto(api);
    setSystemFields(principal, existing, updated);

    dtoManager.update(updated);
    return updated;
  }

  /**
   * the PUT api performs an edit on an existing object by replacing its contents entirely.
   * Override this method to choose which fields need to carry forward from the old object to the
   * new.
   *
   * For example, after edit,
   * - one cannot change the id, create time, etc.
   * - also, the system decides the update time, updatedBy, etc.
   *
   * @param principal user performing the update
   * @param existing the old object
   * @param updated the updated object (which is yet to be persisted)
   */
  protected void setSystemFields(
      final ThirdEyePrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    updated
        .setId(existing.getId())
        .setCreateTime(existing.getCreateTime())
        .setCreatedBy(existing.getCreatedBy())
        .setUpdatedBy(principal.getName())
        .setUpdateTime(new Timestamp(new Date().getTime()));
  }

  protected DtoT toDto(final ApiT api) {
    throw new UnsupportedOperationException("Not implemented");
  }

  protected abstract ApiT toApi(final DtoT dto);

  protected DtoT get(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @Context UriInfo uriInfo
  ) {
    authService.authenticate(authHeader);

    final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    final List<DtoT> results = queryParameters.size() > 0
        ? dtoManager.filter(new DaoFilterBuilder(apiToBeanMap).buildFilter(queryParameters))
        : dtoManager.findAll();

    return respondOk(results.stream().map(this::toApi));
  }

  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApiT> list) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(list, "Invalid request");

    return respondOk(list.stream()
        .peek(this::validate)
        .map(api -> createDto(principal, api))
        .peek(dtoManager::save)
        .map(this::toApi)
        .collect(Collectors.toList())
    );
  }

  @PUT
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApiT> list) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    return respondOk(list.stream()
        .peek(this::validate)
        .map(o -> updateDto(principal, o))
        .peek(dtoManager::update)
        .map(this::toApi)
        .collect(Collectors.toList()));
  }

  @GET
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    authService.authenticate(authHeader);
    return respondOk(toApi(get(id)));
  }

  @DELETE
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final DtoT dto = dtoManager.findById(id);
    if (dto != null) {
      dtoManager.delete(dto);
      log.warn(String.format("Deleted id: %d by principal: %s", id, principal));

      return respondOk(toApi(dto));
    }

    return respondOk(statusResponse(ERR_OBJECT_DOES_NOT_EXIST, id));
  }
}
