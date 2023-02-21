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
package ai.startree.thirdeye.plugins.bootstrap.opencore;

import static ai.startree.thirdeye.spi.util.FileUtils.readJsonObjectsFromResourcesFolder;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.template.TemplatePropertyMetadata;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The goal of this class is to maintain a list of description for commonly used properties.
 */
public class CommonProperties {

  // lazy loaded
  private static final String COMMON_PROPERTIES_PATH = "common-properties";

  // lazy loaded
  public static Map<String, TemplatePropertyMetadata> NAME_TO_PROPERTY;

  private Map<String, TemplatePropertyMetadata> nameToProperty() {
    if (NAME_TO_PROPERTY == null) {
      final TemplatePropertyMetadata[] commonProperties = readJsonObjectsFromResourcesFolder(
          COMMON_PROPERTIES_PATH,
          this.getClass(),
          TemplatePropertyMetadata[].class).get(0);
      NAME_TO_PROPERTY = Arrays.stream(commonProperties)
          .collect(Collectors.toMap(TemplatePropertyMetadata::getName, e -> e));
    }
    return NAME_TO_PROPERTY;
  }

  public void enrichCommonProperties(final List<AlertTemplateApi> templates) {
    for (final AlertTemplateApi template : templates) {
      final List<TemplatePropertyMetadata> properties = template.getProperties();
      if (properties == null) {
        continue;
      }
      for (final TemplatePropertyMetadata p : properties) {
        final TemplatePropertyMetadata commonProperty = nameToProperty().get(p.getName());
        if (commonProperty == null) {
          continue;
        }
        // hand coded mapping because getting this exact behavior was not easy with MapStruct
        // behaviour: don't erase what's existing - don't try to map every field
        if (p.getDescription() == null) {
          p.setDescription(commonProperty.getDescription());
        }
        if (p.getOptions() == null) {
          p.setOptions(commonProperty.getOptions());
        }
        if (p.isMultiselect() == null) {
          p.setMultiselect(commonProperty.isMultiselect());
        }
        if (p.getJsonType() == null) {
          p.setJsonType(commonProperty.getJsonType());
        }
        if (p.getStep() == null) {
          p.setStep(commonProperty.getStep());
        }
        if (p.getSubStep() == null) {
          p.setSubStep(commonProperty.getSubStep());
        }
      }
    }
  }
}
