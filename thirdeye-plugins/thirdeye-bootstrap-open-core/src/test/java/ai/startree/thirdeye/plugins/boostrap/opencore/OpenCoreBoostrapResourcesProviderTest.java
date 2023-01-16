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
package ai.startree.thirdeye.plugins.boostrap.opencore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProvider;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import java.util.List;
import org.testng.annotations.Test;

public class OpenCoreBoostrapResourcesProviderTest {

  // ensures the generation of percentile templates is not broken
  // has to be updated every time a template is added
  @Test
  public void testNumberOfTemplates() {
    final OpenCoreBoostrapResourcesProvider provider = new OpenCoreBoostrapResourcesProvider();
    final List<AlertTemplateApi> templates = provider.getAlertTemplates();
    assertThat(templates.size()).isEqualTo(8);
    assertThat(
        templates.stream().filter(t -> t.getName().contains("-percentile")).count()).isEqualTo(4);
  }
}
