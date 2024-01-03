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
package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Map;

public class StringTemplateUtils {

  @SuppressWarnings("unchecked")
  public static <T> T applyContext(final T template,
      final Map<String, Object> valuesMap)
      throws IOException, ClassNotFoundException {
    final Module module = new SimpleModule()
        .addSerializer(Templatable.class, new TemplateEngineTemplatableSerializer(valuesMap))
        .addSerializer(String.class, new TemplateEngineStringSerializer(valuesMap));
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(module);

    // serialize as json - properties are applied during the serialization
    final String jsonString = objectMapper.writeValueAsString(template);
    return (T) objectMapper.readValue(jsonString, template.getClass());
  }
}
