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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_ID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_NAME;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.ensureNull;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.DaoFilterBuilder;
import ai.startree.thirdeye.RequestCache;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.api.CountApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudService<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(CrudService.class);
  protected final AuthorizationManager authorizationManager;

  protected final AbstractManager<DtoT> dtoManager;
  protected final ImmutableMap<String, String> apiToIndexMap;

  public CrudService(final AuthorizationManager authorizationManager,
      final AbstractManager<DtoT> dtoManager,
      final ImmutableMap<String, String> apiToIndexMap) {
    this.authorizationManager = authorizationManager;
    this.dtoManager = dtoManager;
    this.apiToIndexMap = ImmutableMap.<String, String>builder()
        .put("id", "baseId")
        .putAll(apiToIndexMap)
        .build();
  }

  // FIXME SUVODEEP MAIN DESIGN QUESTION ON WHETHER ONLY IF namespace match AND or  if  
  public ApiT get(
      final ThirdEyeServerPrincipal principal,
      final Long id) {
    final DtoT dto = getDto(id);
    authorizationManager.ensureCanRead(principal, dto);

    final RequestCache cache = createRequestCache();
    return toApi(dto, cache);
  }

  /**
   * Get the dto by id
   * CAUTION: does not ensure authorization.
   *
   * @param id id
   * @return dto
   */
  // FIXME CYRIL authz - ENSURE AUTHORIZATION IN DOWNSTREAM CONSUMERS
  protected DtoT getDto(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

   
  @Deprecated
  public ApiT findByName(
      final ThirdEyeServerPrincipal principal,
      final String name) {
    final RequestCache cache = createRequestCache();
    ensureExists(name, ERR_MISSING_NAME);

    /* If name column is mapped, use the mapping, else use 'name' */
    final String nameColumn = optional(apiToIndexMap.get("name")).orElse("name");
    final List<DtoT> byName = dtoManager.filter(new DaoFilter()
        .setPredicate(Predicate.EQ(nameColumn, name)));

    // FIXME CYRIL authz - CANNOT BE RESOLVED CORRECTLY WITHOUT KNOWING THE CURRENT NAMESPACE 
    //  cc Suvodeep for instance if a user has access to two namespaces and the two namespaces have a datasource called "pinot", then there is no way to know which one should be returned
    //  best would be to remove this method
    ensure(byName.size() > 0, ERR_OBJECT_DOES_NOT_EXIST, name);
    if (byName.size() > 1) {
      throw serverError(ERR_UNKNOWN, "Error. Multiple objects with name: " + name);
    }
    final DtoT dtoT = byName.iterator().next();
    authorizationManager.ensureCanRead(principal, dtoT);
    return toApi(dtoT, cache);
  }

  public Stream<ApiT> list(
      final ThirdEyeServerPrincipal principal,
      final MultivaluedMap<String, String> queryParameters
  ) {
    final List<DtoT> results = queryParameters.size() > 0
        ? dtoManager.filter(new DaoFilterBuilder(apiToIndexMap).buildFilter(queryParameters))
        : dtoManager.findAll();
    // FIXME CYRIL ADD namespace in-app filter - then add query level filter

    final RequestCache cache = createRequestCache();
    return results.stream()
        .filter(dto -> authorizationManager.hasAccess(principal, dto, AccessType.READ))
        .map(dto -> toApi(dto, cache));
  }

  @NonNull
  public List<ApiT> createMultiple(final ThirdEyePrincipal principal,
      final List<ApiT> list) {
    final RequestCache cache = createRequestCache();
    final List<ApiT> result = list.stream()
        .peek(api -> validate(principal, api, null))
        .map(this::toDto)
        .peek(dto -> authorizationManager.enrichNamespace(principal, dto))
        .peek(dto -> authorizationManager.ensureCanCreate(principal, dto))
        .map(dto -> setSystemFields(principal, dto))
        .peek(dto -> prepareCreatedDto(principal, dto))
        .peek(dtoManager::save)
        .peek(dto -> Objects.requireNonNull(dto.getId(), "DB update failed!"))
        .peek(this::postCreate)
        .map(dto -> toApi(dto, cache))
        .collect(Collectors.toList());
    authorizationManager.invalidateCache();
    return result;
  }

  @NonNull
  public List<ApiT> editMultiple(final ThirdEyeServerPrincipal principal,
      final List<ApiT> list) {
    final RequestCache cache = createRequestCache();
    final List<ApiT> result = list.stream()
        .map(o -> updateDto(principal, o))
        .peek(dtoManager::update)
        .peek(dto -> this.postUpdate(principal, dto))
        .map(dto -> toApi(dto, cache))
        .collect(Collectors.toList());
    authorizationManager.invalidateCache();
    return result;
  }

  private DtoT updateDto(final ThirdEyeServerPrincipal principal, final ApiT api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final DtoT existing = ensureExists(dtoManager.findById(id));
    validate(principal, api, existing);

    final DtoT updated = toDto(api);

    // Check the that user can modify the resource before and after the update.
    authorizationManager.ensureCanEdit(principal, existing, updated);

    // Allow downstream classes to process any additional changes
    prepareUpdatedDto(principal, existing, updated);

    // Override system fields.
    updated.setId(id);
    setSystemFields(principal, existing, updated);

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
      final ThirdEyeServerPrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    // By default, do nothing.
  }

  /**
   * Override this method to set default values to a dto being created.
   * You can also throw in this method to prevent the creation of dto.
   */
  protected void prepareCreatedDto(final ThirdEyePrincipal principal, final DtoT dto) {
    // By default, do nothing.
  }

  private DtoT setSystemFields(final ThirdEyeServerPrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    updated.setCreatedBy(existing.getCreatedBy())
        .setCreateTime(existing.getCreateTime())
        .setUpdatedBy(principal.getName())
        .setUpdateTime(new Timestamp(new Date().getTime()));
    return updated;
  }

  public ApiT delete(final ThirdEyeServerPrincipal principal, final Long id) {
    final DtoT dto = dtoManager.findById(id);
    if (dto != null) {
      authorizationManager.ensureCanDelete(principal, dto);

      deleteDto(dto);
      LOG.warn(String.format("Deleted id: %d by principal: %s", id, principal.getName()));

      final RequestCache cache = createRequestCache();
      return toApi(dto, cache);
    }

    return null;
  }

  // FIXME CYRIL ADD NAMESPACE FILTER   
  public void deleteAll(final ThirdEyeServerPrincipal principal) {
    dtoManager.findAll()
        .stream()
        .peek(dto -> authorizationManager.ensureCanDelete(principal, dto))
        .forEach(this::deleteDto);
  }

  // FIXME CYRIL ADD NAMESPACE FILTER  
  public CountApi count(final ThirdEyeServerPrincipal principal, final MultivaluedMap<String, String> queryParameters) {
    final CountApi api = new CountApi();
    final Long count = queryParameters.size() > 0
        ? dtoManager.count(new DaoFilterBuilder(apiToIndexMap).buildFilter(queryParameters)
        .getPredicate())
        : dtoManager.count();
    api.setCount(count);
    return api;
  }

  /**
   * Initialize Request Cache
   *
   * @return cache container
   */
  protected RequestCache createRequestCache() {
    return new RequestCache();
  }

  /**
   * @param dto dto
   * @param cache optional cache parameter
   * @return convert to api object
   */
  protected ApiT toApi(final DtoT dto, final RequestCache cache) {
    return toApi(dto);
  }

  /**
   * No cache implementation
   *
   * @param dto dto
   * @return convert to api object
   */
  protected ApiT toApi(final DtoT dto) {
    throw new UnsupportedOperationException("Either of the 2 toApi variants must be implemented!");
  }

  protected DtoT toDto(final ApiT api) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Validate create/edit. On Creation the existing dto will be null
   *
   * @param api The request api object
   * @param existing the existing dto. On Creation the existing dto will be null
   */
  protected void validate(final ThirdEyePrincipal principal, final ApiT api, @Nullable final DtoT existing) {
    if (existing == null) {
      ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    }
  }

  private DtoT setSystemFields(final ThirdEyePrincipal principal, final DtoT dto) {
    final Timestamp currentTime = new Timestamp(new Date().getTime());
    dto
        .setCreatedBy(principal.getName())
        .setCreateTime(currentTime)
        .setUpdatedBy(principal.getName())
        .setUpdateTime(currentTime);
    return dto;
  }

  protected void postUpdate(final ThirdEyePrincipal principal, final DtoT dto) {
    // default is a no-op
  }

  protected void postCreate(final DtoT dtoT) {
    // default is a no-op
  }

  protected void deleteDto(final DtoT dto) {
    dtoManager.delete(dto);
  }
}
