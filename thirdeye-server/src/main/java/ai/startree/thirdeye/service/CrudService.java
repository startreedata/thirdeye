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

import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.ResourceUtils.ensureNull;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_ID;

import ai.startree.thirdeye.DaoFilterUtils;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.api.CountApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudService<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(CrudService.class);
  public static final ImmutableMultivaluedMap<String, String> EMPTY_PARAMS = ImmutableMultivaluedMap.empty();
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

  public ApiT get(
      final ThirdEyeServerPrincipal principal,
      final Long id) {
    final DtoT dto = getDto(id);
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanRead(principal, dto);

    return toApi(dto);
  }

  /**
   * Get the dto by id
   * CAUTION: does not ensure authorization.
   *
   * @param id id
   * @return dto
   */
  protected DtoT getDto(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

  public Stream<ApiT> list(
      final ThirdEyeServerPrincipal principal,
      final MultivaluedMap<String, String> queryParameters
  ) {
    final String namespace = authorizationManager.currentNamespace(principal);
    final DaoFilter daoFilter = DaoFilterUtils.buildFilter(queryParameters, apiToIndexMap,
        namespace);
    final List<DtoT> results = dtoManager.filter(daoFilter);

    return results.stream()
        .filter(dto -> authorizationManager.canRead(principal, dto))
        .map(this::toApi);
  }

  @NonNull
  public List<ApiT> createMultiple(final ThirdEyePrincipal principal,
      final List<ApiT> list) {
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
        .map(this::toApi)
        .collect(Collectors.toList());
    if (!result.isEmpty()) {
      authorizationManager.invalidateCache(result.getFirst().namespace(), result.getFirst().getClass());
    }
    return result;
  }

  @NonNull
  public List<ApiT> editMultiple(final ThirdEyeServerPrincipal principal,
      final List<ApiT> list) {
    final List<ApiT> result = list.stream()
        .map(o -> updateDto(principal, o))
        .peek(dtoManager::update)
        .peek(dto -> this.postUpdate(principal, dto))
        .map(this::toApi)
        .collect(Collectors.toList());
    if (!result.isEmpty()) {
      authorizationManager.invalidateCache(result.getFirst().namespace(), result.getFirst().getClass());
    }
    return result;
  }

  private DtoT updateDto(final ThirdEyeServerPrincipal principal, final ApiT api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final DtoT existing = ensureExists(dtoManager.findById(id));
    authorizationManager.ensureNamespace(principal, existing);
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
      authorizationManager.ensureNamespace(principal, dto);
      authorizationManager.ensureCanDelete(principal, dto);
      deleteDto(dto);
      LOG.warn("Deleted id: {} by principal: {}", id, principal.getName());
      authorizationManager.invalidateCache(dto.namespace(), dto.getClass());
      return toApi(dto);
    }

    return null;
  }

  // FIXME CYRIL scale - highly inefficient for tasks 
  public void deleteAll(final ThirdEyeServerPrincipal principal) {
    final String namespace = authorizationManager.currentNamespace(principal);
    final DaoFilter daoFilter = DaoFilterUtils.buildFilter(EMPTY_PARAMS, apiToIndexMap,
        namespace);
    final List<DtoT> entities = dtoManager.filter(daoFilter);
    for (final DtoT dto : entities) {
      // todo authz cyril - consider a filter rather than throwing - a user should be able to delete all entities she can access 
      authorizationManager.ensureCanDelete(principal, dto);
      // todo cyril - add a method to delete multiple entities at once - this loop is extremely slow
      deleteDto(dto);
      LOG.warn("Deleted id: {} by principal: {}", dto.getId(), principal.getName());
    }
    if (!entities.isEmpty()) {
      authorizationManager.invalidateCache(namespace, entities.getFirst().getClass());
    }
  }

  public CountApi count(final ThirdEyeServerPrincipal principal,
      final MultivaluedMap<String, String> queryParameters) {
    final String namespace = authorizationManager.currentNamespace(principal);
    final DaoFilter daoFilter = DaoFilterUtils.buildFilter(queryParameters, apiToIndexMap,
        namespace);
    final Long count = dtoManager.count(daoFilter.getPredicate());
    return new CountApi().setCount(count);
  }

  /**
   * No cache implementation
   *
   * @param dto dto
   * @return convert to api object
   */
  protected ApiT toApi(final DtoT dto) {
    throw new UnsupportedOperationException("toApi not implemented. Please reach out to support.");
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
