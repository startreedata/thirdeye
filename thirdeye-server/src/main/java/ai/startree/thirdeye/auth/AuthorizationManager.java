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
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
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
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
      "thirdeye-internal", RandomStringUtils.random(1024, true, true));

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final AccessControl accessControl;
  private final AlertManager alertManager;
  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;

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
    return INTERNAL_VALID_PRINCIPAL.equals(principal) ||
        accessControl.hasAccess(principal.getAuthToken(), identifier, accessType);
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

  public <T extends AbstractDTO> ResourceIdentifier resourceId(final T dto) {
    final AbstractDTO authParentDto = resolveAuthParentDto(dto);

    return ResourceIdentifier.from(
        optional(authParentDto.getId()).map(Objects::toString).orElse(DEFAULT_NAME),
        optional(authParentDto.getAuth())
            .map(AuthorizationConfigurationDTO::getNamespace).orElse(DEFAULT_NAMESPACE),
        optional(SubEntities.BEAN_TYPE_MAP.get(authParentDto.getClass()))
            .map(Objects::toString).orElse(DEFAULT_ENTITY_TYPE));
  }

  // Resolves the Authorization parent for certain dto classes.
  // AnomalyDTO -> EnumerationItemDTO if exists, else AlertDTO
  // RcaInvestigationDTO -> AnomalyDTO's parent (recursive call)
  // Everything else -> itself
  private <T extends AbstractDTO> AbstractDTO resolveAuthParentDto(final T dto) {
    if (dto.getClass() == AnomalyDTO.class) {
      final var anomalyDto = (AnomalyDTO) (dto);
      if (anomalyDto.getEnumerationItem() != null) {
        var enumItemId = anomalyDto.getEnumerationItem().getId();
        return ensureExists(enumerationItemManager.findById(enumItemId));
      }

      var alertId = anomalyDto.getDetectionConfigId();
      return ensureExists(alertManager.findById(alertId));
    }

    if (dto.getClass() == RcaInvestigationDTO.class) {
      final var rcaDto = (RcaInvestigationDTO) (dto);
      var anomalyId = rcaDto.getAnomaly().getId();
      return resolveAuthParentDto(ensureExists(anomalyManager.findById(anomalyId)));
    }

    return dto;
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
            accessType, principal.getName(), resourceIdentifier.entityType, resourceIdentifier.name)
    ).build());
  }

  public static ThirdEyePrincipal getInternalValidPrincipal() {
    return INTERNAL_VALID_PRINCIPAL;
  }
}
