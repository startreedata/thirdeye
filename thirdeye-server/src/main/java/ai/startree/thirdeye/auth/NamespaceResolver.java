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
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class NamespaceResolver {

  private final AlertManager alertManager;
  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;

  private final Cache<Long, @NonNull String> namespaceCache = CacheBuilder.newBuilder()
      .maximumSize(2048)
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .build();

  @Inject
  public NamespaceResolver(final AlertManager alertManager, final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager) {
    this.alertManager = alertManager;
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
  }

  public void invalidateCache() {
    namespaceCache.invalidateAll();
  }

  public @NonNull String resolveNamespace(final AbstractDTO dto) {
    if (dto == null) {
      return DEFAULT_NAMESPACE;
    }
    if (dto instanceof AnomalyDTO) {
      return resolveAnomalyNamespace((AnomalyDTO) (dto));
    } else if (dto instanceof RcaInvestigationDTO) {
      return resolveRcaNamespace((RcaInvestigationDTO) (dto));
    } else {
      return getNamespaceFromAuth(dto);
    }
  }

  private @NonNull String resolveAnomalyNamespace(final AnomalyDTO dto) {
    if (dto.getEnumerationItem() != null) {
      return optional(dto.getEnumerationItem())
          .map(AbstractDTO::getId)
          .map(this::getEnumerationItemNamespaceById)
          .orElse(DEFAULT_NAMESPACE);
    }

    return optional(dto.getDetectionConfigId())
        .map(this::getAlertNamespaceById)
        .orElse(DEFAULT_NAMESPACE);
  }

  private @NonNull String resolveRcaNamespace(final RcaInvestigationDTO dto) {
    return optional(dto.getAnomaly())
        .map(AbstractDTO::getId)
        .map(this::getAnomalyNamespaceById)
        .orElse(DEFAULT_NAMESPACE);
  }

  private @NonNull String getEnumerationItemNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () ->
          optional(enumerationItemManager.findById(id))
              .map(this::getNamespaceFromAuth)
              .orElse(DEFAULT_NAMESPACE));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private @NonNull String getAlertNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () ->
          optional(alertManager.findById(id))
              .map(this::getNamespaceFromAuth)
              .orElse(DEFAULT_NAMESPACE));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private @NonNull String getAnomalyNamespaceById(final long id) {
    try {
      return namespaceCache.get(id, () -> optional(id)
          .map(anomalyManager::findById)
          .map(this::resolveAnomalyNamespace)
          .orElse(DEFAULT_NAMESPACE));
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
  
  private @NonNull String getNamespaceFromAuth(final AbstractDTO dto) {
    return optional(dto)
        .map(AbstractDTO::getAuth)
        .map(AuthorizationConfigurationDTO::getNamespace)
        .orElse(DEFAULT_NAMESPACE);
  }
}
