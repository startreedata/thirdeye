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

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;

/**
 * The goal of this class is to maintain a list of commonly used description for template properties
 * For such properties, default metadata is provided
 */
public class CommonProperties {

  // please maintain this list in alphabetical order
  public static final Map<String, String> COMMON_DESCRIPTIONS = ImmutableMap.<String, String>builder()
      .put("dataSource", "The Pinot datasource to use.")
      .put("dataset", "The dataset to query.")
      .put("timeColumn",
          "TimeColumn used to group by time. If set to AUTO (the default value), the Pinot primary time column is used.")
      .put("timezone", "Timezone used to group by time.")
      .build();

  public static void enrichCommonProperties(final List<AlertTemplateApi> templates) {
    for (final AlertTemplateApi template : templates) {
      final List<TemplatePropertyMetadata> properties = template.getProperties();
      if (properties == null) {
        continue;
      }
      for (final TemplatePropertyMetadata p : properties) {
        if (p.getDescription() == null) {
          p.setDescription(COMMON_DESCRIPTIONS.get(p.getName()));
        }
        // could set other metadata fields here with other maps
      }
    }
  }
}
