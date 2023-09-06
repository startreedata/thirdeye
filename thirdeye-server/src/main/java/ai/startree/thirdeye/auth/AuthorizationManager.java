/*
 * Copyright 2023 StarTree Inc
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

import static ai.startree.thirdeye.spi.accessControl.ResourceIdentifier.DEFAULT_ENTITY_TYPE;
import static ai.startree.thirdeye.spi.accessControl.ResourceIdentifier.DEFAULT_NAME;
import static ai.startree.thirdeye.spi.accessControl.ResourceIdentifier.DEFAULT_NAMESPACE;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.ThirdEyePrincipal.AuthenticationType;
import ai.startree.thirdeye.datalayer.dao.SubEntities;
import ai.startree.thirdeye.spi.accessControl.AccessControl;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.RandomStringUtils;

public class AuthorizationManager {

  // A meta-resource to authorize thirdeye automation/maintenance.
  static final ResourceIdentifier ROOT_RESOURCE_ID = ResourceIdentifier.from("thirdeye-root",
      "thirdeye-root",
      "thirdeye-root");

  private static final ThirdEyePrincipal INTERNAL_VALID_PRINCIPAL = new ThirdEyePrincipal(
      "thirdeye-internal", RandomStringUtils.random(1024, true, true), AuthenticationType.INTERNAL);

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final AccessControl accessControl;
  private final AlertManager alertManager;
  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;

  // cache resource id's to their respective auth namespaces
  private final Cache<Long, String> namespaceCache = CacheBuilder.newBuilder()
      .maximumSize(2048)
      .expireAfterAccess(5, TimeUnit.SECONDS)
      .build();

  @Inject
  public AuthorizationManager(
      final AlertTemplateRenderer alertTemplateRenderer,
      final AccessControl accessControl,
      final AlertManager alertManager,
      final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.accessControl = accessControl;
    this.alertManager = alertManager;
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
  }

  public <T extends AbstractDTO> void ensureCanCreate(final ThirdEyePrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.WRITE);
    relatedEntities(entity).forEach(relatedId ->
        ensureHasAccess(principal, relatedId, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanDelete(final ThirdEyePrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.WRITE);
  }

  public <T extends AbstractDTO> void ensureCanEdit(final ThirdEyePrincipal principal,
      final T before, final T after) {
    ensureHasAccess(principal, resourceId(before), AccessType.WRITE);
    ensureHasAccess(principal, resourceId(after), AccessType.WRITE);
    relatedEntities(after).forEach(related ->
        ensureHasAccess(principal, related, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanRead(final ThirdEyePrincipal principal,
      final T entity) {
    ensureHasAccess(principal, resourceId(entity), AccessType.READ);
  }

  public void ensureCanValidate(final ThirdEyePrincipal principal, final AlertDTO entity) {
    ensureCanCreate(principal, entity);
  }

  public <T extends AbstractDTO> void ensureHasAccess(final ThirdEyePrincipal principal,
      final T entity, final AccessType accessType) {
    ensureHasAccess(principal, resourceId(entity), accessType);
  }

  public void ensureHasAccess(final ThirdEyePrincipal principal,
      final ResourceIdentifier identifier, final AccessType accessType) {
    if (!hasAccess(principal, identifier, accessType)) {
      throw forbiddenExceptionFor(principal, identifier, accessType);
    }
  }

  public <T extends AbstractDTO> boolean canRead(final ThirdEyePrincipal principal, final T entity) {
    return hasAccess(principal, resourceId(entity), AccessType.READ);
  }

  public <T extends AbstractDTO> boolean hasAccess(final ThirdEyePrincipal principal,
      final T entity, final AccessType accessType) {
    return hasAccess(principal, resourceId(entity), accessType);
  }

  public boolean hasAccess(final ThirdEyePrincipal principal,
      final ResourceIdentifier identifier, final AccessType accessType) {
    if (INTERNAL_VALID_PRINCIPAL.equals(principal)) {
      return true;
    } else if (principal.getAuthenticationType() == AuthenticationType.BASIC_AUTH) {
      return true;
    } else {
      return accessControl.hasAccess(principal.getAuthToken(), identifier, accessType);
    }
  }

  public void ensureHasRootAccess(final ThirdEyePrincipal principal) {
    if (!hasRootAccess(principal)) {
      throw new ForbiddenException(Response.status(
          Status.FORBIDDEN.getStatusCode(),
          String.format("root access denied to %s", principal.getName())
      ).build());
    }
  }

  public boolean hasRootAccess(final ThirdEyePrincipal principal) {
    return INTERNAL_VALID_PRINCIPAL.equals(principal) ||
        accessControl.hasAccess(principal.getAuthToken(), ROOT_RESOURCE_ID, AccessType.WRITE);
  }

  // Returns the resource identifier for a dto.
  // Null is ok and maps to a default resource id.
  public ResourceIdentifier resourceId(final AbstractDTO dto) {
    final var name = Optional.ofNullable(dto)
        .map(AbstractDTO::getId)
        .map(Objects::toString)
        .orElse(DEFAULT_NAME);

    final var namespace = Optional.ofNullable(dto)
        .map(this::resolveAuthNamespaceFromCache)
        .orElse(DEFAULT_NAMESPACE);

    final var entityType = Optional.ofNullable(dto)
        .map(AbstractDTO::getClass)
        .map(SubEntities.BEAN_TYPE_MAP::get)
        .map(Objects::toString)
        .orElse(DEFAULT_ENTITY_TYPE);

    return ResourceIdentifier.from(name, namespace, entityType);
  }

  // Resolves the Authorization parent for certain dto classes.
  // AnomalyDTO -> EnumerationItemDTO namespace if exists, else AlertDTO namespace
  // RcaInvestigationDTO -> Anomaly's parent namespace
  private String resolveAuthNamespaceFromCache(final AbstractDTO dto) {
    if (dto == null) {
      return DEFAULT_NAMESPACE;
    } else if (dto.getId() == null) {
      return resolveAuthNamespace(dto);
    }

    try {
      return namespaceCache.get(dto.getId(), () -> resolveAuthNamespace(dto));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private String resolveAuthNamespace(final AbstractDTO dto) {
    if (dto instanceof AnomalyDTO) {
      return resolveAnomalyNamespace((AnomalyDTO) (dto));
    }

    if (dto instanceof RcaInvestigationDTO) {
      return resolveRcaNamespace((RcaInvestigationDTO) (dto));
    }

    return Optional.of(dto)
        .map(AbstractDTO::getAuth)
        .map(AuthorizationConfigurationDTO::getNamespace)
        .orElse(DEFAULT_NAMESPACE);
  }

  private String resolveAnomalyNamespace(final AnomalyDTO dto) {
    if (dto.getEnumerationItem() != null) {
      return Optional.of(dto.getEnumerationItem())
          .map(AbstractDTO::getId)
          .map(enumerationItemManager::findById)
          .map(this::resolveAuthNamespaceFromCache)
          .orElse(DEFAULT_NAMESPACE);
    }

    return Optional.of(dto)
        .map(AnomalyDTO::getDetectionConfigId)
        .map(alertManager::findById)
        .map(this::resolveAuthNamespaceFromCache)
        .orElse(DEFAULT_NAMESPACE);
  }

  private String resolveRcaNamespace(final RcaInvestigationDTO dto) {
    return Optional.of(dto)
        .map(RcaInvestigationDTO::getAnomaly)
        .map(AbstractDTO::getId)
        .map(anomalyManager::findById)
        .map(this::resolveAuthNamespaceFromCache)
        .orElse(DEFAULT_NAMESPACE);
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
      final ThirdEyePrincipal principal,
      final ResourceIdentifier resourceIdentifier,
      final AccessType accessType
  ) {
    return new ForbiddenException(Response.status(
        Status.FORBIDDEN.getStatusCode(),
        String.format("%s access denied to %s for entity %s %s",
            accessType, principal.getName(), resourceIdentifier.getEntityType(), resourceIdentifier.getName())
    ).build());
  }

  public static ThirdEyePrincipal getInternalValidPrincipal() {
    return INTERNAL_VALID_PRINCIPAL;
  }
}
