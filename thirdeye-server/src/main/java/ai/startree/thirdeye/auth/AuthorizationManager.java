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

import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_ENTITY_TYPE;
import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.authorize;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.datalayer.dao.SubEntities;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.RandomStringUtils;

public class AuthorizationManager {

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

  @Inject
  public AuthorizationManager(
      final AlertTemplateRenderer alertTemplateRenderer,
      final ThirdEyeAuthorizer thirdEyeAuthorizer,
      final NamespaceResolver namespaceResolver) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.thirdEyeAuthorizer = thirdEyeAuthorizer;
    this.namespaceResolver = namespaceResolver;

    // TODO CYRIL next PR - make this configurable in server.yaml - for the moment the codepath is disabled
    this.requireNamespace = false;
  }

  /**
   * Set a namespace in the entity if necessary, based on the principal.
   * Note: following the review, we decided to put this logic outside
   * {@link AuthorizationManager#ensureCanCreate}.
   * FIXME CYRIL REVIEW AGAIN WITH SUVODEEP 
   * Should always be called before {@link AuthorizationManager#ensureCanCreate},  
   * {@link AuthorizationManager#ensureCanEdit}, {@link AuthorizationManager#ensureCanValidate}
   */
  // TODO CYRIL next PR - use this method in the Service layer
  public <T extends AbstractDTO> void enrichNamespace(final ThirdEyeServerPrincipal principal,
      final T entity) {
    // enrich with the namespace if it is not set and required
    if (requireNamespace) {
      if (!namespaceIsSet(entity)) {
        final List<String> namespaces = thirdEyeAuthorizer.listNamespaces(principal);
        authorize(!namespaces.isEmpty());
        if (namespaces.size() == 1) {
          entity.setAuth(new AuthorizationConfigurationDTO().setNamespace(namespaces.get(0)));
        } else if (namespaces.size() > 1) {
          throw new NotAuthorizedException(String.format(
              "Namespace not provided and cannot be resolved automatically. "
                  + "Please provide a namespace explicitly. Namespaces: %s", namespaces));
        }
      }
    } else {
      // do not enrich namespaces on the fly
      return;
    }
  }

  // TODO DON'T UPDATE HERE
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

    final var namespace = namespaceResolver.resolveNamespace(dto);

    final var entityType = optional(dto)
        .map(AbstractDTO::getClass)
        .map(SubEntities.BEAN_TYPE_MAP::get)
        .map(Objects::toString)
        .orElse(DEFAULT_ENTITY_TYPE);

    return ResourceIdentifier.from(name, namespace, entityType);
  }

  private <T extends AbstractDTO> List<ResourceIdentifier> relatedEntities(T entity) {
    if (entity instanceof AlertDTO) {
      final AlertDTO alertDto = (AlertDTO) entity;
      final AlertTemplateDTO alertTemplateDTO = alertTemplateRenderer.getTemplate(alertDto.getTemplate());
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
            accessType, principal.getName(), resourceIdentifier.getEntityType(), resourceIdentifier.getName())
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
