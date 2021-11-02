package org.apache.pinot.thirdeye.resources;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.Constants.NO_AUTH_USER;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_ID;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureNull;
import static org.apache.pinot.thirdeye.util.ResourceUtils.respondOk;
import static org.apache.pinot.thirdeye.util.ResourceUtils.statusResponse;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.ThirdEyeCrudApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AbstractManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudResource<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger log = LoggerFactory.getLogger(CrudResource.class);

  protected final AbstractManager<DtoT> dtoManager;
  protected final ImmutableMap<String, String> apiToBeanMap;

  @Inject
  public CrudResource(
      final AbstractManager<DtoT> dtoManager,
      final ImmutableMap<String, String> apiToBeanMap) {
    this.dtoManager = dtoManager;
    this.apiToBeanMap = apiToBeanMap;
  }

  /**
   * Validate create/edit. On Creation the existing dto will be null
   *
   * @param api The request api object
   * @param existing the existing dto. On Creation the existing dto will be null
   */
  protected void validate(final ApiT api, @Nullable final DtoT existing) {
    if (existing == null) {
      ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    }
  }

  protected abstract DtoT createDto(final ThirdEyePrincipal principal, final ApiT api);

  private DtoT updateDto(final ThirdEyePrincipal principal, final ApiT api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final DtoT existing = ensureExists(dtoManager.findById(id));
    validate(api, existing);

    final DtoT updated = toDto(api);

    // Override system fields.
    updated
        .setId(existing.getId())
        .setCreateTime(existing.getCreateTime())
        .setCreatedBy(existing.getCreatedBy())
        .setUpdatedBy(principal.getName())
        .setUpdateTime(new Timestamp(new Date().getTime()));

    // Allow downstream classes to process any additional changes
    prepareUpdatedDto(principal, existing, updated);

    // ready to be persisted to db.
    return updated;
  }

  /**
   * the PUT api performs an edit on an existing object by replacing its contents entirely.
   * Override this method to choose which fields need to carry forward from the old object to the
   * new. The system decides the update time, updatedBy, etc.
   *
   * For example, after edit,
   * - An alert might need to have a default cron or have the lastTimestamp copied over.
   *
   * @param principal user performing the update
   * @param existing the old object
   * @param updated the updated object (which is yet to be persisted)
   */
  protected void prepareUpdatedDto(
      final ThirdEyePrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    // By default, do nothing.
  }

  protected DtoT toDto(final ApiT api) {
    throw new UnsupportedOperationException("Not implemented");
  }

  protected abstract ApiT toApi(final DtoT dto);

  protected DtoT get(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

  protected void deleteDto(DtoT dto) {
    dtoManager.delete(dto);
  }

  @GET
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @Context UriInfo uriInfo
  ) {
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
    ensureExists(list, "Invalid request");

    return respondOk(list.stream()
        .peek(api1 -> validate(api1, null))
        .map(api -> createDto(new ThirdEyePrincipal(NO_AUTH_USER), api))
        .peek(dtoManager::save)
        .peek(dto -> requireNonNull(dto.getId(), "DB update failed!"))
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
    return respondOk(list.stream()
        .map(o -> updateDto(new ThirdEyePrincipal(NO_AUTH_USER), o))
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
    return respondOk(toApi(get(id)));
  }

  @DELETE
  @Path("{id}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final DtoT dto = dtoManager.findById(id);
    if (dto != null) {
      deleteDto(dto);
      log.warn(String.format("Deleted id: %d by principal: %s", id, NO_AUTH_USER));

      return respondOk(toApi(dto));
    }

    return respondOk(statusResponse(ERR_OBJECT_DOES_NOT_EXIST, id));
  }

  @DELETE
  @Path("/all")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader) {
    dtoManager.findAll().forEach(this::deleteDto);
    return Response.ok().build();
  }
}
