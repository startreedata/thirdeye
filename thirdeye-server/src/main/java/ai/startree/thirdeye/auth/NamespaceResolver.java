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
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
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
 * TODO CYRIL when requireNamespace = true is true, it is not possible to change the namespace of an entity
 *  and all entities should have a namespace directly - no need to inherit
 */
@Deprecated // namespace will be resolved at write time 
@Singleton
public class NamespaceResolver {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceResolver.class);

  private final AlertManager alertManager;
  private final SubscriptionGroupManager subscriptionGroupDao;

  private final Cache<Long, @NonNull Optional<String>> namespaceCache = CacheBuilder.newBuilder()
      .maximumSize(2048)
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .build();

  @Inject
  public NamespaceResolver(final AlertManager alertManager,
      final SubscriptionGroupManager subscriptionGroupDao) {
    this.alertManager = alertManager;
    this.subscriptionGroupDao = subscriptionGroupDao;
  }

  public void invalidateCache() {
    namespaceCache.invalidateAll();
  }

  // should match with the doc https://dev.startree.ai/docs/get-started-with-thirdeye/access-control-in-thirdeye#namespaces-for-thirdeye-resources
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
    } else if (dto instanceof DatasetConfigDTO datasetDto) {
      namespace = resolveDatasetDtoNamespace(datasetDto);
    } else if (dto instanceof MetricConfigDTO metricConfigDTO) {
      namespace = resolveMetricConfigDtoNamespace(metricConfigDTO);
    } else if (dto instanceof DataSourceDTO || dto instanceof AlertTemplateDTO
        || dto instanceof AlertDTO || dto instanceof SubscriptionGroupDTO
        || dto instanceof EventDTO) {
      namespace = getNamespaceFromAuth(dto);
    } else {
      // please define cases explicitly as above - keeping this codepath to prevent workspace leaks and find changes, but should not happen
      LOG.error("Limited namespace support for {} entity. Please reach out to StarTree support.",
          optional(dto).map(AbstractDTO::getClass).orElse(null));
      namespace = getNamespaceFromAuth(dto);
    }

    // FIXME CYRIL authz if requireNamespace is set - WARN if there is SOMETHING in DEFAULT NAMESPACE

    return namespace.orElse(DEFAULT_NAMESPACE);
  }

  private @NonNull Optional<String> resolveMetricConfigDtoNamespace(
      final @Nullable MetricConfigDTO dto) {
    return getNamespaceFromAuth(dto);
  }

  private @NonNull Optional<String> resolveDatasetDtoNamespace(final DatasetConfigDTO dto) {
    return getNamespaceFromAuth(dto);
  }

  private @NonNull Optional<String> resolveTaskDtoNamespace(final @Nullable TaskDTO dto) {
    if (dto == null) {
      return Optional.empty();
    }
    if (dto.namespace() != null) {
      // namespace was set at write time
      return optional(dto.namespace());
    }
    final Long refId = Objects.requireNonNull(dto.getRefId(),
        String.format("TaskDto %s has no refId. This should never happen", dto.getId()));
    return switch (dto.getTaskType()) {
      case DETECTION -> getAlertNamespaceById(refId);
      case NOTIFICATION -> getSubscriptionGroupNamespaceById(refId);
    };
  }

  private @NonNull Optional<String> resolveEnumerationItemNamespace(
      final @Nullable EnumerationItemDTO dto) {
    if (dto == null) {
      return Optional.empty();
    }
    return optional(dto.namespace());
  }

  private @NonNull Optional<String> resolveAnomalyNamespace(final @Nullable AnomalyDTO dto) {
    if (dto == null) {
      return Optional.empty();
    }
    return optional(dto.namespace());
  }

  private @NonNull Optional<String> resolveRcaNamespace(final @NonNull RcaInvestigationDTO dto) {
    return optional(dto.namespace());
  }

  private @NonNull Optional<String> getAlertNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () -> getNamespaceFromAuth(alertManager.findById(id)));
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
    return optional(dto).map(AbstractDTO::namespace);
  }
}
