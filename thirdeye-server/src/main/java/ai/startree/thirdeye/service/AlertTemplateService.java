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

import static ai.startree.thirdeye.util.ResourceUtils.ensure;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.core.BootstrapResourcesRegistry;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertTemplateService extends CrudService<AlertTemplateApi, AlertTemplateDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(AlertTemplateService.class);

  private final BootstrapResourcesRegistry bootstrapResourcesRegistry;

  @Inject
  public AlertTemplateService(final AlertTemplateManager alertTemplateManager,
      final AuthorizationManager authorizationManager,
      final BootstrapResourcesRegistry bootstrapResourcesRegistry) {
    super(authorizationManager, alertTemplateManager, ImmutableMap.of());
    this.bootstrapResourcesRegistry = bootstrapResourcesRegistry;
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final AlertTemplateApi api,
      @Nullable final AlertTemplateDTO existing) {
    super.validate(principal, api, existing);
    final long count = api.getNodes()
        .stream()
        .filter(p -> "Enumerator".equals(p.getType())) // TODO spyne - use enumerator node type
        .count();
    ensure(count <= 1, "Max 1 enumerator node supported at this time. found: " + count);
  }

  @Override
  protected AlertTemplateDTO toDto(final AlertTemplateApi api) {
    return ApiBeanMapper.toAlertTemplateDto(api);
  }

  @Override
  protected AlertTemplateApi toApi(final AlertTemplateDTO dto) {
    return ApiBeanMapper.toAlertTemplateApi(dto);
  }
  
  // fixme cyril authz related: override validate and prevent duplicate name?  

  public List<AlertTemplateApi> loadRecommendedTemplates(final ThirdEyeServerPrincipal principal,
      final boolean updateExisting) {
    final String namespace = authorizationManager.currentNamespace(principal);
    return loadRecommendedTemplates(principal, updateExisting, namespace);
  }

  // protected to be available to other services that need to inject the namespace manually
  @NonNull
  protected List<AlertTemplateApi> loadRecommendedTemplates(final ThirdEyeServerPrincipal principal,
      final boolean updateExisting, final String explicitNamespace) {
    LOG.info("Loading recommended templates in namespace {}: START.", explicitNamespace);
    final List<AlertTemplateApi> alertTemplates = bootstrapResourcesRegistry.getAlertTemplates();
    LOG.info("Loading recommended templates in namespace {}: templates to load: {}",
        explicitNamespace,
        alertTemplates.stream().map(AlertTemplateApi::getName).collect(Collectors.toList()));
    // inject namespace in entities to create/update
    alertTemplates.forEach(e -> e.setAuth(new AuthorizationConfigurationApi().setNamespace(explicitNamespace)));
    final List<AlertTemplateApi> loadedTemplates = loadTemplates(principal, alertTemplates,
        updateExisting);
    LOG.info("Loading recommended templates in namespace {}: SUCCESS. Templates loaded: {}",
        explicitNamespace,
        loadedTemplates.stream().map(AlertTemplateApi::getName).collect(Collectors.toList()));

    return loadedTemplates;
  }

  private List<AlertTemplateApi> loadTemplates(final ThirdEyeServerPrincipal principal,
      final List<AlertTemplateApi> alertTemplates, final boolean updateExisting) {
    final List<AlertTemplateApi> toCreateTemplates = new ArrayList<>();
    final List<AlertTemplateApi> toUpdateTemplates = new ArrayList<>();
    for (final AlertTemplateApi templateApi : alertTemplates) {
      final AlertTemplateDTO existingTemplate = dtoManager.findUniqueByNameAndNamespace(
          templateApi.getName(), templateApi.getAuth().getNamespace());
      if (existingTemplate == null) {
        toCreateTemplates.add(templateApi);
      } else {
        templateApi.setId(existingTemplate.getId());
        toUpdateTemplates.add(templateApi);
      }
    }

    final List<AlertTemplateApi> upserted = createMultiple(principal, toCreateTemplates);
    if (updateExisting) {
      final List<AlertTemplateApi> updated = editMultiple(principal, toUpdateTemplates);
      upserted.addAll(updated);
    }
    return upserted;
  }
}
