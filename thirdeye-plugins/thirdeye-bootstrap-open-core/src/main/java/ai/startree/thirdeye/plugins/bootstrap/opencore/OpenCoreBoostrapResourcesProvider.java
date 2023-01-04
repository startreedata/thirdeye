/*
 * Copyright 2022 StarTree Inc
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OpenCoreBoostrapResourcesProvider implements BootstrapResourcesProvider {

  private static final String RESOURCES_TEMPLATES_PATH = "alert-templates";

  @Override
  public List<AlertTemplateApi> getAlertTemplates() {
    final List<AlertTemplateApi> templates = readJsonObjectsFromResourcesFolder(
        RESOURCES_TEMPLATES_PATH,
        this.getClass(),
        AlertTemplateApi.class);

    final List<AlertTemplateApi> percentileTemplates = templates.stream()
        .map(PercentileTemplateCreator::createPercentileVariant)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    templates.addAll(percentileTemplates);

    CommonProperties.enrichCommonProperties(templates);

    return templates;
  }
}
