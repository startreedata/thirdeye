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
import java.util.concurrent.TimeUnit;

public class StringTemplateUtils {

  // FIXME CYRIL - this is extremely slow because an object mapper is recreated at each time
  // this means the ser/deserializer for are recreated by reflection at each call.  
  // TODO v1: 
  //   the templateEngineTemplatableSerializer values should be mutable
  //   at call time, this means the function need to be synchronized - one instance should only be used by one thread at the same time
  // v2: concurrent queue of object mappers, TemplatableSerializer, StringSerializer to avoid locks on this class
  // a pool of 3-4 

  private static final LinkedBlockingDeque<TemplateMapper> mapperQueue = new LinkedBlockingDeque<>();
  private static final int mapperCount = 1;
  public static final int MAX_MAPPERS = 4;

  static {
    mapperQueue.add(new TemplateMapper());
  }

  @SuppressWarnings("unchecked")
  public static <T> T applyContext(final T template, final Map<String, Object> valuesMap)
      throws IOException {
    // should never block - if this blocks, the number of templatableMapper should be made bigger
    // check size, if zero an num instance created is small  
    TemplateMapper templateMapper = null;
    try {
      if (mapperQueue.isEmpty() && TemplateMapper.numInstanceCreated < MAX_MAPPERS) {
        templateMapper = new TemplateMapper();
      } else {
        templateMapper = mapperQueue.pollLast(10, TimeUnit.MILLISECONDS); 
      }
      templateMapper.setValuesMap(valuesMap);
      // serialize as json - properties are applied during the serialization
      final String jsonString = templateMapper.writeValueAsString(template);
      return (T) templateMapper.readValue(jsonString, template.getClass());
    } finally {
      if (templateMapper != null) {
        mapperQueue.add(templateMapper);   
      }
    }
  }

  // not thread safe - should only be accessed by one thread at a time ! 
  private static class TemplateMapper {

    private static int numInstanceCreated = 0;

    private final ObjectMapper objectMapper;
    private final TemplateEngineTemplatableSerializer templatableSerializer;
    private final TemplateEngineStringSerializer stringSerializer;

    private TemplateMapper() {
      templatableSerializer = new TemplateEngineTemplatableSerializer(Collections.emptyMap());
      stringSerializer = new TemplateEngineStringSerializer(Collections.emptyMap());
      final Module module = new SimpleModule().addSerializer(Templatable.class,
          templatableSerializer).addSerializer(String.class, stringSerializer);
      objectMapper = new ObjectMapper().registerModule(module);
      numInstanceCreated++;
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
