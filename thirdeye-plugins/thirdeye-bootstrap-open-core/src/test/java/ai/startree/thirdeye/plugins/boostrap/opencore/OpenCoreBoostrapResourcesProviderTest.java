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
package ai.startree.thirdeye.plugins.boostrap.opencore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProvider;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.dto.TemplateConfigurationDTO;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.testng.annotations.Test;

public class OpenCoreBoostrapResourcesProviderTest {

  // ensures the generation of percentile templates is not broken
  // has to be updated every time a template is added
  @Test
  public void testNumberOfTemplates() {
    final OpenCoreBoostrapResourcesProvider provider = new OpenCoreBoostrapResourcesProvider();
    int queryLimitStatement = 200_000; 
    final TemplateConfigurationDTO templateConfiguration = new TemplateConfigurationDTO().setSqlLimitStatement(queryLimitStatement);
    final List<AlertTemplateApi> templates = provider.getAlertTemplates(templateConfiguration);
    assertThat(templates.size()).isEqualTo(8);
    assertThat(
        templates.stream().filter(t -> t.getName().contains("-percentile")).count()).isEqualTo(4);
    for (final AlertTemplateApi t: templates) {
      final Optional<TemplatePropertyMetadata> queryLimit = t.getProperties().stream()
          .filter(p -> "queryLimit".equals(p.getName()))
          .findFirst();
      assertThat(queryLimit).isPresent();
      assertThat(queryLimit.get().getDefaultValue()).isEqualTo(queryLimitStatement);
    }
  }
}
