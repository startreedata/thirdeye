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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringTemplateUtils {

  private static final Logger LOG = LoggerFactory.getLogger(StringTemplateUtils.class);
  // see explanation of the design in thirdeye-benchmarks/benchmark_notes
  private static final LinkedBlockingDeque<TemplateMapper> MAPPER_QUEUE = new LinkedBlockingDeque<>();
  private static final int NUM_MAPPERS = 8;
  private static final int NUM_MAPPERS_WARNING = 12;

  static {
    for (int i = 0; i < NUM_MAPPERS; i++) {
      MAPPER_QUEUE.add(new TemplateMapper());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T applyContext(final T template, final Map<String, Object> valuesMap)
      throws IOException {
    TemplateMapper templateMapper = null;
    try {
      templateMapper = MAPPER_QUEUE.pollLast();
      if (templateMapper == null) {
        // the template mapper will have to build its ser/deser cache, so it will be slow at the beginning
        templateMapper = new TemplateMapper();
        if (TemplateMapper.instanceNumber > NUM_MAPPERS_WARNING) {
          // if this happens to often, there is a memory leak
          LOG.error(
              "Failed to obtain a template mapper instance. Current number of mapper instance: {}. Instantiating a new instance. Please reach out to support if the number of instance keeps growing.",
              TemplateMapper.instanceNumber);
        }
      }
      templateMapper.setValuesMap(valuesMap);
      // serialize as json - properties are applied during the serialization
      final String jsonString = templateMapper.writeValueAsString(template);
      return (T) templateMapper.readValue(jsonString, template.getClass());
    } finally {
      if (templateMapper != null) {
        MAPPER_QUEUE.addLast(templateMapper);
      }
    }
  }

  /**
   * Delegate pattern on objectMapper with an additional method setValuesMap to update the values
   * to inject when rendering templates.
   * Not thread safe - should only be accessed by one thread at a time !
   */
  private static class TemplateMapper {

    private static int instanceNumber = 0;

    private final ObjectMapper objectMapper;
    private final TemplateEngineTemplatableSerializer templatableSerializer;
    private final TemplateEngineStringSerializer stringSerializer;

    private TemplateMapper() {
      templatableSerializer = new TemplateEngineTemplatableSerializer(Collections.emptyMap());
      stringSerializer = new TemplateEngineStringSerializer(Collections.emptyMap());
      final Module module = new SimpleModule().addSerializer(Templatable.class,
          templatableSerializer).addSerializer(String.class, stringSerializer);
      objectMapper = new ObjectMapper().registerModule(module);
      instanceNumber++;
    }

    private TemplateMapper setValuesMap(final Map<String, Object> valuesMap) {
      templatableSerializer.setValuesMap(valuesMap);
      stringSerializer.setValuesMap(valuesMap);
      return this;
    }

    private String writeValueAsString(final Object value) throws JsonProcessingException {
      return objectMapper.writeValueAsString(value);
    }

    private <T> T readValue(final String content, final Class<T> valueType)
        throws JsonProcessingException {
      return objectMapper.readValue(content, valueType);
    }
  }
}
