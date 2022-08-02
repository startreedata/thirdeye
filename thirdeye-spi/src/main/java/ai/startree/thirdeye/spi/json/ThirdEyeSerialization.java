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
package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Provides serialization resources.
 * ThirdEye implements a custom (de)serialization to simulate the Union type for {@link Templatable}
 * fields.
 * See {@link ApiTemplatableSerializer} and {@link ApiTemplatableDeserializer}
 *
 * In most context (API level, API json reading/writing, persistence level), you should use  {@link
 * #newObjectMapper newObjectMapper} to get an ObjectMapper.
 * If you need a jackson.databind.Module with the ThirdEye specific (de)serializations, use {@link
 * #TEMPLATABLE}.
 */
public class ThirdEyeSerialization {

  /**
   * Serialization module for ThirdEye.
   * Implements union type for {@link Templatable}.
   */
  public static final Module TEMPLATABLE = new SimpleModule()
      .addSerializer(Templatable.class, new ApiTemplatableSerializer())
      .addDeserializer(Templatable.class, new ApiTemplatableDeserializer())
      .addSerializer(TemplatableMap.class, new TemplatableMapSerializer())
      .addDeserializer(TemplatableMap.class, new TemplatableMapDeserializer())
      ;

  /**
   * Returns an objectMapper that implements all Thirdeye specific (de)serialization.
   * Use this method instead of new ObjectMapper();
   */
  public static ObjectMapper newObjectMapper() {
    return new ObjectMapper().registerModule(TEMPLATABLE);
  }
}
