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

import static ai.startree.thirdeye.spi.auth.ResourceIdentifier.DEFAULT_NAMESPACE;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Long term, should not be necessary anymore when requireNamespace = true;
 */
@Singleton
public class NamespaceResolver {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceResolver.class);

  private final AlertManager alertManager;
  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final SubscriptionGroupManager subscriptionGroupDao;

  private final Cache<Long, @NonNull Optional<String>> namespaceCache = CacheBuilder.newBuilder()
      .maximumSize(2048)
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .build();

  @Inject
  public NamespaceResolver(final AlertManager alertManager,
      final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager,
      final SubscriptionGroupManager subscriptionGroupDao) {
    this.alertManager = alertManager;
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.subscriptionGroupDao = subscriptionGroupDao;
  }

  public void invalidateCache() {
    namespaceCache.invalidateAll();
  }

  public @NonNull String resolveNamespace(final @Nullable AbstractDTO dto) {
    Optional<String> namespace;
    if (dto instanceof AnomalyDTO anomalyDto) {
      namespace = resolveAnomalyNamespace(anomalyDto);
    } else if (dto instanceof RcaInvestigationDTO rcaInvestigationDto) {
      namespace = resolveRcaNamespace(rcaInvestigationDto);
    } else if (dto instanceof EnumerationItemDTO enumerationItemDto) {
      namespace = resolveEnumerationItemNamespace(enumerationItemDto);
    } else if (dto instanceof TaskDTO taskDto) {
      namespace = resolveTaskDtoNamespace(taskDto);
    } else {
      namespace = getNamespaceFromAuth(dto);
    }

    return namespace.orElse(DEFAULT_NAMESPACE);
  }

  private Optional<String> resolveTaskDtoNamespace(final TaskDTO taskDto) {
    final Long refId = Objects.requireNonNull(taskDto.getRefId(), String.format(
        "TaskDto %s has no refId. This should never happen", taskDto.getId()
    ));
    return switch (taskDto.getTaskType()) {
      case DETECTION -> getAlertNamespaceById(refId);
      case NOTIFICATION -> getSubscriptionGroupNamespaceById(refId);
    };
  }

  private @NonNull Optional<String> resolveEnumerationItemNamespace(
      final @Nullable EnumerationItemDTO enumerationItemDTO) {
    if (enumerationItemDTO == null) {
      return Optional.empty();
    }
    final Long enumerationItemId = optional(enumerationItemDTO.getId()).orElse(null);
    if (enumerationItemId != null) {
      final Optional<String> enumNamespace = getEnumerationItemNamespaceById(enumerationItemId);
      if (enumNamespace.isPresent()) {
        return enumNamespace;
      }
    }
    final Long detectionConfigId = optional(enumerationItemDTO.getAlert()).map(AbstractDTO::getId)
        .orElse(null);
    if (detectionConfigId != null) {
      return getAlertNamespaceById(detectionConfigId);
    }

    return getNamespaceFromAuth(enumerationItemDTO);
  }

  private @NonNull Optional<String> resolveAnomalyNamespace(final @Nullable AnomalyDTO dto) {
    if (dto == null) {
      return Optional.empty();
    }
    // anomaly inherits namespace from enum
    final Optional<String> enumNamespace = resolveEnumerationItemNamespace(
        dto.getEnumerationItem());
    if (enumNamespace.isPresent()) {
      return enumNamespace;
    }
    // if no enum or enum has no namespace, fallback to detection config namespace
    final Long detectionConfigId = dto.getDetectionConfigId();
    if (detectionConfigId != null) {
      return getAlertNamespaceById(detectionConfigId);
    }
    return Optional.empty();
  }

  private @NonNull Optional<String> resolveRcaNamespace(final @NonNull RcaInvestigationDTO dto) {
    final Long anomalyId = optional(dto.getAnomaly()).map(AbstractDTO::getId).orElse(null);
    if (anomalyId != null) {
      return getAnomalyNamespaceById(anomalyId);
    }
    return Optional.empty();
  }

  private @NonNull Optional<String> getEnumerationItemNamespaceById(final long id) {
    try {
      return namespaceCache.get(id,
          () -> getNamespaceFromAuth(enumerationItemManager.findById(id)));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private @NonNull Optional<String> getAlertNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () -> getNamespaceFromAuth(alertManager.findById(id)));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private @NonNull Optional<String> getAnomalyNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () -> resolveAnomalyNamespace(anomalyManager.findById(id)));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<String> getSubscriptionGroupNamespaceById(final Long id) {
    try {
      return namespaceCache.get(id, () -> getNamespaceFromAuth(subscriptionGroupDao.findById(id)));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private @NonNull Optional<String> getNamespaceFromAuth(final @Nullable AbstractDTO dto) {
    return optional(dto).map(AbstractDTO::getAuth).map(AuthorizationConfigurationDTO::getNamespace);
  }
}
