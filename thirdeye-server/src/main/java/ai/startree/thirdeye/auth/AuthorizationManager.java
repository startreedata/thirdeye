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

import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
  private static final ResourceIdentifier ROOT_RESOURCE_ID = ResourceIdentifier.from("thirdeye-root",
      "thirdeye-root",
      "thirdeye-root");

  private static final ThirdEyeServerPrincipal INTERNAL_VALID_PRINCIPAL = new ThirdEyeServerPrincipal(
      "thirdeye-internal", RandomStringUtils.random(1024, true, true), AuthenticationType.INTERNAL);

  private final DataSourceManager datasourceDao;
  private final DatasetConfigManager datasetConfigDao;
  private final AlertTemplateManager alertTemplateDao;
  private final AlertManager alertDao;
  private final ThirdEyeAuthorizer thirdEyeAuthorizer;

  private final LoadingCache<Long, Optional<AnomalyDTO>> anomalyCache;
  private final LoadingCache<AlertTemplateNamespace, Optional<AlertTemplateDTO>> templateCache;
  
  private record AlertTemplateNamespace(AlertTemplateDTO alertTemplate, @Nullable String namespace){}

  private final static Map<Class<? extends AbstractDTO>, SubEntityType> DTO_TO_ENTITY_TYPE;

  static {
    // could be independent of BEAN_TYPE_MAP but for the moment the code is the same
    DTO_TO_ENTITY_TYPE = new HashMap<>(BEAN_TYPE_MAP);
    DTO_TO_ENTITY_TYPE.put(TaskDTO.class, SubEntityType.TASK);
  }

  @Inject
  public AuthorizationManager(
      final DataSourceManager datasourceDao,
      final DatasetConfigManager datasetConfigDao,
      final AlertTemplateManager alertTemplateManager,
      final AlertManager alertManager,
      final AnomalyManager anomalyDao,
      final ThirdEyeAuthorizer thirdEyeAuthorizer) {
    this.datasourceDao = datasourceDao;
    this.datasetConfigDao = datasetConfigDao;
    this.alertTemplateDao = alertTemplateManager;
    this.alertDao = alertManager;
    this.thirdEyeAuthorizer = thirdEyeAuthorizer;

    // very simple caching - todo cyril authz review details
    anomalyCache = CacheBuilder.newBuilder()
        .maximumSize(2048)
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build(new CacheLoader<>() {
          @Override
          public Optional<AnomalyDTO> load(final Long key) {
            return optional(anomalyDao.findById(key));
          }
        });
    templateCache = CacheBuilder.newBuilder()
        .maximumSize(2048)
        .expireAfterWrite(60, TimeUnit.SECONDS)
        .build(new CacheLoader<>() {
          @Override
          public Optional<AlertTemplateDTO> load(final AlertTemplateNamespace key) {
            return optional(alertTemplateDao.findMatchInNamespaceOrUnsetNamespace(
                key.alertTemplate(), key.namespace()));
          }
        });
  }

  /**
   * Set a namespace in the entity if necessary, based on the principal.
   * Note: following the review, this logic is put outside
   * {@link AuthorizationManager#ensureCanCreate}
   * {@link AuthorizationManager#ensureCanCreate}.
   * Should always be called before {@link AuthorizationManager#ensureCanCreate}.
   */
  public <T extends AbstractDTO> void enrichNamespace(final ThirdEyePrincipal principal,
      final @NonNull T entity) {
    if (!namespaceIsSet(entity)) {
      final String currentNamespace = currentNamespace(principal);
      entity.setAuth(new AuthorizationConfigurationDTO().setNamespace(currentNamespace));
    }
    // else namespace is passed explicitly - don't modify it
  }

  // TODO CYRIL authz should be moved down to ThirdEyeAuthorizer once we implement multi-namespace UI support?
  public @Nullable String currentNamespace(final ThirdEyePrincipal principal) {
    final List<String> namespaces = thirdEyeAuthorizer.listNamespaces(principal);
    if (namespaces.size() == 0) {
      throw new ForbiddenException("Access Denied.");  // throw 403
    } else if (namespaces.size() == 1) {
      return namespaces.get(0);
    } else {
      // todo cyril authz - resolve from principal
      throw new NotAuthorizedException(String.format(
          "Namespace not cannot be resolved automatically. Please provide a namespace explicitly. Namespaces: %s",
          namespaces));
    }
  }

  public <T extends AbstractDTO> void ensureCanCreate(final ThirdEyePrincipal principal,
      final T entity) {
    ensureCanAccess(principal, resourceId(entity), AccessType.WRITE);
    relatedEntities(entity).forEach(relatedId ->
        ensureCanAccess(principal, relatedId, AccessType.READ));
  }

  // used in CrudService
  public <T extends AbstractDTO> boolean canRead(final ThirdEyePrincipal principal,
      final T entity) {
    return canAccess(principal, resourceId(entity), AccessType.READ) &&
        relatedEntities(entity).stream()
            .allMatch(e -> canAccess(principal, e, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanRead(final ThirdEyePrincipal principal,
      final T entity) {
    if (!canRead(principal, entity)) {
      throw forbiddenExceptionFor(principal, resourceId(entity), AccessType.READ);
    }
  }

  public <T extends AbstractDTO> void ensureCanEdit(final ThirdEyePrincipal principal,
      final @NonNull T before, final @NonNull T after) {
    ensureCanAccess(principal, resourceId(before), AccessType.WRITE);
    ensureCanAccess(principal, resourceId(after), AccessType.WRITE);
    authorize(Objects.equals(before.namespace(), after.namespace()),
        String.format(
            "Entity namespace cannot change. Existing namespace: %s. New namespace: %s",
            before.getAuth(),
            after.getAuth()));
    relatedEntities(after).forEach(related ->
        ensureCanAccess(principal, related, AccessType.READ));
  }

  public <T extends AbstractDTO> void ensureCanDelete(final ThirdEyePrincipal principal,
      final T entity) {
    ensureCanAccess(principal, resourceId(entity), AccessType.WRITE);
    relatedEntities(entity).forEach(relatedId ->
        ensureCanAccess(principal, relatedId, AccessType.READ));
  }

  private void ensureCanAccess(final ThirdEyePrincipal principal,
      final ResourceIdentifier identifier, final AccessType accessType) {
    if (!canAccess(principal, identifier, accessType)) {
      throw forbiddenExceptionFor(principal, identifier, accessType);
    }
  }

  private boolean canAccess(final ThirdEyePrincipal principal,
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
  private static ResourceIdentifier resourceId(final AbstractDTO dto) {
    if (dto == null) {
      // todo cyril authz - add NonNull annotation to dto param and all upstream
      return ResourceIdentifier.NULL_IDENTIFIER;
    }

    final String name = optional(dto.getId())
        .map(Objects::toString)
        .orElse(DEFAULT_NAME);
    final String namespace = dto.namespace();
    final String entityType = optional(dto.getClass())
        .map(DTO_TO_ENTITY_TYPE::get)
        .map(Objects::toString)
        .orElse(DEFAULT_ENTITY_TYPE);

    return ResourceIdentifier.from(name, namespace, entityType);
  }

  /**
   * FIXME CYRIL authz - the fetching of entities makes the system slow - introduce some caching on the DAOs? Like 30 seconds caching?
   *
   * For the moment this method is responsible for checking whether related entities are in the
   * same namespace - todo cyril authz - is this the right place ?
   *
   * This method returns all related dependencies in a hierarchical fashion.
   * See hierarchy here https://app.excalidraw.com/s/6rIIm06x9LN/7HD6QC1KRzZ
   **/
  private <T extends AbstractDTO> Set<ResourceIdentifier> relatedEntities(T entity) {
    final Set<ResourceIdentifier> res = new HashSet<>();
    addRelatedEntities(entity, res);
    return res;
  }

  // recursive - caching on DAOs or even the related entities directly will be necessary?
  // todo authz replace checkArgument by specific http code error
  private <T extends AbstractDTO> void addRelatedEntities(final T entity,
      final Set<ResourceIdentifier> result) {
    if (entity instanceof final AlertDTO alertDto) {
      final AlertTemplateDTO alertTemplateDTO = templateCache.getUnchecked(new AlertTemplateNamespace(alertDto.getTemplate(), alertDto.namespace()))
          .orElse(null);
      checkArgument(alertTemplateDTO != null,
          "Invalid template %s. Not found in alert namespace %s.",
          alertDto.getTemplate(), alertDto.namespace()); 
      final ResourceIdentifier resourceId = resourceId(alertTemplateDTO);
      if (!result.contains(resourceId)) {
        result.add(resourceId);
        addRelatedEntities(alertTemplateDTO, result);
      }
      // hack: find the related dataset by looking at the property value directly - assume the key is "dataset"
      // TODO cyril authz - fix this consider rendering the template? but make sure it's not too slow - 
      //  also it causes an issue because it means we need to use the template even if access to the template has not been checked yet
      final @Nullable Object datasetName = optional(alertDto.getProperties()).map(
          e -> e.get("dataset")).orElse(null);
      if (datasetName instanceof String datasetNameStr) {
        final DatasetConfigDTO datasetConfigDTO = datasetConfigDao.findUniqueByNameAndNamespace(
            datasetNameStr, alertDto.namespace());
        checkArgument(datasetConfigDTO != null,
            "Invalid dataset name %s. Not found in alert namespace %s.",
            datasetNameStr, alertDto.namespace());
        final ResourceIdentifier resourceIdDataset = resourceId(datasetConfigDTO);
        if (!result.contains(resourceIdDataset)) {
          result.add(resourceIdDataset);
          addRelatedEntities(datasetConfigDTO, result);
        }
      } else {
        LOG.error("Could not find dataset in alert configuration {}", alertDto);
      }
    } else if (entity instanceof SubscriptionGroupDTO subscriptionGroupDto) {
      final List<AlertAssociationDto> alertAssociations = optional(
          subscriptionGroupDto.getAlertAssociations()).orElse(Collections.emptyList());
      final Set<Long> alertIds = alertAssociations.stream()
          // getAlert.getId is never null
          .map(aa -> aa.getAlert().getId())
          .collect(Collectors.toSet());
      // hack we ensure alerts belong to the same namespace here 
      // this should be done in some validate step but the current validate step does not have the namespace context TODO authz re-design
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

        final ResourceIdentifier resourceId = resourceId(alert);
        if (!result.contains(resourceId)) {
          result.add(resourceId);
          addRelatedEntities(alert, result);
        }
      }
      // todo cyril authz in theory we should also do enumeration items
    } else if (entity instanceof RcaInvestigationDTO rcaInvestigationDTO) {
      final @NonNull Long anomalyId = rcaInvestigationDTO.getAnomaly().getId();
      final AnomalyDTO anomaly =  anomalyCache.getUnchecked(anomalyId).orElse(null);
      // the error message is the same whether the anomaly does not exist in db or the anomaly is in another namespace - this is to avoid leaking anomaly ids of other namespaces
      checkArgument(
          anomaly != null && Objects.equals(rcaInvestigationDTO.namespace(), anomaly.namespace()),
          "Invalid anomaly id or rcaInvestigation namespace %s and anomaly namespace do not match for anomaly id %s.",
          rcaInvestigationDTO.namespace(), anomalyId);
      final ResourceIdentifier resourceId = resourceId(anomaly);
      if (!result.contains(resourceId)) {
        result.add(resourceId);
        addRelatedEntities(anomaly, result);
      }
    } else if (entity instanceof AnomalyDTO anomalyDto) {
      final @NonNull Long alertId = anomalyDto.getDetectionConfigId();
      final AlertDTO alertDto = alertDao.findById(alertId);
      checkArgument(
          alertDto != null && Objects.equals(anomalyDto.namespace(), alertDto.namespace()),
          "Invalid alert id or anomaly namespace %s and alert namespace do not match for alert id %s.",
          anomalyDto.namespace(), alertId);
      final ResourceIdentifier resourceId = resourceId(alertDto);
      if (!result.contains(resourceId)) {
        result.add(resourceId);
        addRelatedEntities(alertDto, result);
      }
      // todo cyril authz implement enumeration item related entity anomalyDto.getEnumerationItem
    } else if (entity instanceof DatasetConfigDTO datasetConfigDTO) {
      // todo cyril authz - make dataset point to datasource by id not name
      final String datasourceName = datasetConfigDTO.getDataSource();
      final DataSourceDTO datasourceDto = datasourceDao.findUniqueByNameAndNamespace(datasourceName, datasetConfigDTO.namespace());
      checkArgument(datasourceDto != null,
          "Invalid datasource name %s. Not found in dataset namespace %s.",
          datasourceName, datasetConfigDTO.namespace());
      final ResourceIdentifier resourceId = resourceId(datasourceDto);
      if (!result.contains(resourceId)) {
        result.add(resourceId);
        addRelatedEntities(datasourceDto, result);
      }
    }
    // todo authz taskDto, metricDto, etc...  
  }

  private static ForbiddenException forbiddenExceptionFor(
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

  private static <T extends AbstractDTO> boolean namespaceIsSet(final @NonNull T entity) {
    return entity.getAuth() != null && entity.getAuth().getNamespace() != null;
  }
}
