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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import static ai.startree.thirdeye.spi.util.FileUtils.readJsonObjectsFromResourcesFolder;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProvider;
import ai.startree.thirdeye.spi.datalayer.dto.TemplateConfigurationDTO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OpenCoreBoostrapResourcesProvider implements BootstrapResourcesProvider {

  public static final String RESOURCES_TEMPLATES_PATH = "alert-templates";

  @Override
  public List<AlertTemplateApi> getAlertTemplates(
      final @NonNull TemplateConfigurationDTO templateConfiguration) {
    final List<AlertTemplateApi> templates = readJsonObjectsFromResourcesFolder(
        RESOURCES_TEMPLATES_PATH,
        this.getClass(),
        AlertTemplateApi.class);

    final List<AlertTemplateApi> percentileTemplates = templates.stream()
        .map(PercentileTemplateCreator::createPercentileVariant)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    templates.addAll(percentileTemplates);

    new CommonProperties().enrichCommonProperties(templates);

    applyTemplateConfiguration(templates, templateConfiguration);

    return templates;
  }
  
  // public to be reused by other template plugins 
  public static void applyTemplateConfiguration(final List<AlertTemplateApi> templates,
      final @NonNull TemplateConfigurationDTO templateConfiguration) {
    // apply default SQL LIMIT statement value 
    for (final AlertTemplateApi alertTemplateApi : templates) {
      alertTemplateApi.getProperties()
          .stream()
          // WARNING: match on property name directly - ensure this naming convention is followed in new templates 
          .filter(p -> "queryLimit".equals(p.getName()))
          .forEach(p -> p.setDefaultValue(templateConfiguration.getSqlLimitStatement()));
    }
  }
}
