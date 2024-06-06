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
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final AlertManager alertDao;
  private final AnomalyManager anomalyDao;
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
      final AlertManager alertManager, 
      final AnomalyManager anomalyManager,
      final ThirdEyeAuthorizer thirdEyeAuthorizer,
      final NamespaceResolver namespaceResolver,
      final AuthConfiguration authConfiguration) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.alertDao = alertManager;
    this.anomalyDao = anomalyManager;
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
      List<T> filtered = entities
          .stream()
          .filter(e -> Objects.equals(explicitNamespace, e.namespace())).toList();
      if (!entities.isEmpty() && filtered.isEmpty()) {
        // to keep backward compatibility with different legacy namespace setups, if requireNamespace is not true 
        // and the filter by namespace filters everything, then try to re-run a filtering on the null namespace 
        // this may leak some entities but the legacy leak entities anyway - so better not to break existing setups
        // if user wants correct namespacing they requireNamespace should be set to true
        filtered = entities
            .stream()
            .filter(e -> e.namespace() == null).toList();
        if (!filtered.isEmpty()) {
          LOG.warn(
              "No entities matching namespace {} in {}. Some entities have their namespace undefined. "
                  + "Returning entities with an undefined namespace.", explicitNamespace, entities);
        }
      }
      return filtered;
    }
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
        // fixme cyril authz design issue - it's not clear to me why the chain of dependency is not resolved
        // eg: checking a read access on an alert does not check for read access on template - see also ensureCanRead 
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

  public <T extends AbstractDTO> boolean canRead(final ThirdEyePrincipal principal,
      final T entity) {
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
      return thirdEyeAuthorizer.authorize(principal, identifier, accessType);
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
        thirdEyeAuthorizer.authorize(principal, ROOT_RESOURCE_ID, AccessType.WRITE);
  }

  // Returns the resource identifier for a dto.
  // Null is ok and maps to a default resource id.
  public ResourceIdentifier resourceId(final AbstractDTO dto) {
    final String name = optional(dto)
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

  /**
   * for the moment this method is responsible for checking whether related entities are in the same
   * namespace - todo cyril authz - don't think it's the right place - consider refactor after migration
   *
   * Note: there can be chains of dependencies
   * eg a RcaInvestigationDto --> AnomalyDTO --> AlertDto --> DatasetDto --> DatasourceDto
   *                                                      --> AlertTemplateDto
   * This method should only return entities that are directly referenced by the entity (the
   * references that can be changed when mutating the entity).
   * For instance, for RcaInvestigationDto, only return AnomalyDTO
   * For AnomalyDto, only return AlertDto.
   * fixme authz the chaining resolution behavior is undefined for the moment - will need to get fixed 
   **/
  private <T extends AbstractDTO> List<ResourceIdentifier> relatedEntities(T entity) {
    if (entity instanceof final AlertDTO alertDto) {
      // fixme cyril authz - related entities should be namespaced
      final AlertTemplateDTO alertTemplateDTO = alertTemplateRenderer.getTemplate(
          alertDto.getTemplate());
      // fixme cyril authz design - add datasource/dataset 
      //   nothing actually ensures an alert runs on a dataset/datasource for which the user has read access
      //   dataset is historically provided by a string key so there is not explicit design for this
      return Collections.singletonList(resourceId(alertTemplateDTO));
    } else if (entity instanceof SubscriptionGroupDTO subscriptionGroupDto) {
      final List<AlertAssociationDto> alertAssociations = optional(
          subscriptionGroupDto.getAlertAssociations()).orElse(Collections.emptyList());
      final Set<Long> alertIds = alertAssociations.stream()
          // getAlert.getId is never null
          .map(aa -> aa.getAlert().getId())
          .collect(Collectors.toSet());
      // hack we ensure alerts belong to the same namespace here 
      // this should be done in some validate step but the current validate step does not have the namespace context FIXME design
      final List<AlertDTO> alertDtos = alertIds.stream()
          .map(alertDao::findById)
          .filter(
              Objects::nonNull) // we ignore null here - deleting an alert should not break a subscription group here - it seems it is allowed in other places in the codebase
          .toList();
      for (final AlertDTO alert : alertDtos) {
        // cannot print the alert id in the error message because it would potentially leak the namespace of an alert for which authz has not been performed yet
        checkArgument(Objects.equals(subscriptionGroupDto.namespace(), alert.namespace()),
            "Subscription namespace %s and alert namespace do not match for alert id %s.",
            subscriptionGroupDto.namespace(), alert.getId());
      }
      // end of hack
      return alertDtos.stream()
          .map(this::resourceId)
          .toList();
    } else if (entity instanceof RcaInvestigationDTO rcaInvestigationDTO) {
      final @NonNull Long anomalyId = rcaInvestigationDTO.getAnomaly().getId(); 
      // same namespace is ensured via RcaInvestigationService#toDto 
      // putting again a check because it's migration code and some legacy entities may not have the property 
      final AnomalyDTO anomaly = anomalyDao.findById(anomalyId);
      checkArgument(Objects.equals(rcaInvestigationDTO.namespace(), anomaly.namespace()), 
          "RcaInvestigation namespace %s and anomaly namespace do not match for anomaly id %s.",
          rcaInvestigationDTO.namespace(), anomaly.getId());
      return List.of(resourceId(anomaly));
    } else if (entity instanceof AnomalyDTO anomalyDTO) {
      // fixme cyril authz - implement
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
