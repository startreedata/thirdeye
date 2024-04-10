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
package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.datalayer.dao.SubEntities.BEAN_TYPE_MAP;
import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_ENTITY_TYPE;
import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.authorize;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationManager {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationManager.class);

  // A meta-resource to authorize thirdeye automation/maintenance.
  static final ResourceIdentifier ROOT_RESOURCE_ID = ResourceIdentifier.from("thirdeye-root",
      "thirdeye-root",
      "thirdeye-root");

  private static final ThirdEyeServerPrincipal INTERNAL_VALID_PRINCIPAL = new ThirdEyeServerPrincipal(
      "thirdeye-internal", RandomStringUtils.random(1024, true, true), AuthenticationType.INTERNAL);

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final ThirdEyeAuthorizer thirdEyeAuthorizer;
  private final NamespaceResolver namespaceResolver;
  private final boolean requireNamespace;

  private final static Map<Class<? extends AbstractDTO>, SubEntityType> DTO_TO_ENTITY_TYPE;

  static {
    // could be independent of BEAN_TYPE_MAP but for the moment the code is the same
    DTO_TO_ENTITY_TYPE = new HashMap<>(BEAN_TYPE_MAP);
    DTO_TO_ENTITY_TYPE.put(TaskDTO.class, SubEntityType.TASK);
  }

  @Inject
  public AuthorizationManager(
      final AlertTemplateRenderer alertTemplateRenderer,
      final ThirdEyeAuthorizer thirdEyeAuthorizer,
      final NamespaceResolver namespaceResolver,
      final AuthConfiguration authConfiguration) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.thirdEyeAuthorizer = thirdEyeAuthorizer;
    this.namespaceResolver = namespaceResolver;

    this.requireNamespace =
        authConfiguration.isEnabled() && authConfiguration.getAuthorization().isRequireNamespace();
  }

  /**
   * Set a namespace in the entity if necessary, based on the principal.
   * Note: following the review, this logic is put outside
   * {@link AuthorizationManager#ensureCanCreate}
   * {@link AuthorizationManager#ensureCanCreate}.
   * Should always be called before {@link AuthorizationManager#ensureCanCreate},
   * {@link AuthorizationManager#ensureCanEdit}, {@link AuthorizationManager#ensureCanValidate}
   */
  public <T extends AbstractDTO> void enrichNamespace(final ThirdEyePrincipal principal,
      final T entity) {
    // enrich with the namespace if it is not set and required
    if (requireNamespace) {
      if (!namespaceIsSet(entity)) {
        final String currentNamespace = currentNamespace(principal);
        entity.setAuth(new AuthorizationConfigurationDTO().setNamespace(currentNamespace));
      } else {
        // namespace is passed explicitly - don't modify it
        return;
      }
    } else {
      // requireNamespace is false - don't enrich
    }
  }

  // TODO CYRIL authz should be moved down to ThirdEyeAuthorizer once we implement multi-namespace UI support?
  // only returns null when requireNamespace is false - Nullable can be removed once migration is done
  public @Nullable String currentNamespace(final ThirdEyePrincipal principal) {
    if (requireNamespace) {
      final List<String> namespaces = thirdEyeAuthorizer.listNamespaces(principal);
      if (namespaces.size() == 0) {
        throw new ForbiddenException("Access Denied.");  // throw 403
      } else if (namespaces.size() == 1) {
        return namespaces.get(0);
      } else {
        throw new NotAuthorizedException(String.format(
            "Namespace not cannot be resolved automatically. Please provide a namespace explicitly. Namespaces: %s",
            namespaces));
      }
    } else {
      return null;
    }
  }

  // FIXME CYRIL I AM HERE - maybe will need a filterByNamespace with an existing dto --> will need to resolve namespace with the namespace resolver
  // TODO CYRIL authz perf - in most cases places using this method should filter at fetch time on the namespace to avoid noisy neighbours effect / stressing the instance   
  public <T extends AbstractDTO> List<T> filterByNamespace(final ThirdEyePrincipal principal,
      final @Nullable String explicitNamespace, final List<T> entities) {
    if (requireNamespace) {
      @NonNull String filteringNamespace = optional(explicitNamespace).orElse(
          Objects.requireNonNull(currentNamespace(principal)));
      return entities.stream()
          .filter(e -> filteringNamespace.equals(namespaceResolver.resolveNamespace(e))).toList();
    } else {
      // namespace is not enforced
      @Nullable String filteringNamespace = optional(explicitNamespace).orElse(null);
      if (filteringNamespace == null) {
        // filter on the "not set" namespace
        return entities.stream()
            .filter(e -> e.getAuth() == null || e.getAuth().getNamespace() == null)
            .toList();
      } else {
        return entities.stream()
            .filter(
                e -> e.getAuth() != null || filteringNamespace.equals(e.getAuth().getNamespace()))
            .toList();
      }
    }
  }

  public <T extends AbstractDTO> void ensureCanCreate(final ThirdEyeServerPrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.WRITE);
    relatedEntities(entity).forEach(relatedId ->
        ensureHasAccess(principal, relatedId, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanDelete(final ThirdEyeServerPrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.WRITE);
  }

  public <T extends AbstractDTO> void ensureCanEdit(final ThirdEyeServerPrincipal principal,
      final T before, final T after) {
    ensureHasAccess(principal, resourceId(before), AccessType.WRITE);
    ensureHasAccess(principal, resourceId(after), AccessType.WRITE);
    // prevent namespace change 
    if (requireNamespace) {
      // namespaces must be set and equal
      // THIS IS INCOMPATIBLE WITH ENUMERATION ITEM NAMESPACE EDITION - so not compatible with some users
      authorize(Objects.equals(before.getAuth(), after.getAuth()),
          String.format(
              "Entity namespace cannot change. Existing namespace: %s. New namespace: %s",
              before.getAuth(),
              after.getAuth()));
    } else {
      // allow namespace editions to keep backward compatibility
    }
    relatedEntities(after).forEach(related ->
        ensureHasAccess(principal, related, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanRead(final ThirdEyeServerPrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.READ);
  }

  public void ensureCanValidate(final ThirdEyeServerPrincipal principal, final AlertDTO entity) {
    ensureCanCreate(principal, entity);
  }

  public <T extends AbstractDTO> void ensureHasAccess(final ThirdEyeServerPrincipal principal,
      final T entity, final AccessType accessType) {
    ensureHasAccess(principal, resourceId(entity), accessType);
  }

  public void ensureHasAccess(final ThirdEyeServerPrincipal principal,
      final ResourceIdentifier identifier, final AccessType accessType) {
    if (!hasAccess(principal, identifier, accessType)) {
      throw forbiddenExceptionFor(principal, identifier, accessType);
    }
  }

  public <T extends AbstractDTO> boolean canRead(final ThirdEyeServerPrincipal principal,
      final T entity) {
    return hasAccess(principal, resourceId(entity), AccessType.READ);
  }

  public <T extends AbstractDTO> boolean hasAccess(final ThirdEyeServerPrincipal principal,
      final T entity, final AccessType accessType) {
    return hasAccess(principal, resourceId(entity), accessType);
  }

  public boolean hasAccess(final ThirdEyeServerPrincipal principal,
      final ResourceIdentifier identifier, final AccessType accessType) {
    if (INTERNAL_VALID_PRINCIPAL.equals(principal)) {
      return true;
    } else if (principal.getAuthenticationType() == AuthenticationType.BASIC_AUTH) {
      return true;
    } else {
      return thirdEyeAuthorizer.authorize(principal, identifier, accessType);
    }
    // TODO CYRIL ADD A case for a PUBLIC identifier for immutable READ ok, WRITE not ok shared resources (eg templates)
  }

  public void ensureHasRootAccess(final ThirdEyeServerPrincipal principal) {
    if (!hasRootAccess(principal)) {
      throw new ForbiddenException(Response.status(
          Status.FORBIDDEN.getStatusCode(),
          String.format("root access denied to %s", principal.getName())
      ).build());
    }
  }

  public boolean hasRootAccess(final ThirdEyePrincipal principal) {
    return INTERNAL_VALID_PRINCIPAL.equals(principal) ||
        thirdEyeAuthorizer.authorize(principal, ROOT_RESOURCE_ID, AccessType.WRITE);
  }

  // Returns the resource identifier for a dto.
  // Null is ok and maps to a default resource id.
  public ResourceIdentifier resourceId(final AbstractDTO dto) {
    final var name = optional(dto)
        .map(AbstractDTO::getId)
        .map(Objects::toString)
        .orElse(DEFAULT_NAME);

    final String namespace = namespaceResolver.resolveNamespace(dto);

    final String entityType = optional(dto)
        .map(AbstractDTO::getClass)
        .map(DTO_TO_ENTITY_TYPE::get)
        .map(Objects::toString)
        .orElse(DEFAULT_ENTITY_TYPE);

    return ResourceIdentifier.from(name, namespace, entityType);
  }

  private <T extends AbstractDTO> List<ResourceIdentifier> relatedEntities(T entity) {
    if (entity instanceof AlertDTO) {
      final AlertDTO alertDto = (AlertDTO) entity;
      final AlertTemplateDTO alertTemplateDTO = alertTemplateRenderer.getTemplate(
          alertDto.getTemplate());
      return Collections.singletonList(resourceId(alertTemplateDTO));
    }
    return new ArrayList<>();
  }

  public ForbiddenException forbiddenExceptionFor(
      final ThirdEyeServerPrincipal principal,
      final ResourceIdentifier resourceIdentifier,
      final AccessType accessType
  ) {
    return new ForbiddenException(Response.status(
        Status.FORBIDDEN.getStatusCode(),
        String.format("%s access denied to %s for entity %s %s",
            accessType, principal.getName(), resourceIdentifier.getEntityType(),
            resourceIdentifier.getName())
    ).build());
  }

  public static ThirdEyeServerPrincipal getInternalValidPrincipal() {
    return INTERNAL_VALID_PRINCIPAL;
  }

  public void invalidateCache() {
    namespaceResolver.invalidateCache();
  }

  private static <T extends AbstractDTO> boolean namespaceIsSet(final T entity) {
    return entity.getAuth() != null && entity.getAuth().getNamespace() != null;
  }
}
