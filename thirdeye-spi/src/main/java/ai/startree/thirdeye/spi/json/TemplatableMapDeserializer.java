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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Map;

/**
 * This Deserializer is used to with the Templatable Deserializer.
 * At the json level, todo cyril add doc
 */
public class TemplatableMapDeserializer extends JsonDeserializer<TemplatableMap<?, ?>> {

  @Override
  public TemplatableMap<?, ?> deserialize(final JsonParser jsonParser,
      final DeserializationContext context) throws IOException {
    return TemplatableMap.fromDelegate(jsonParser.readValueAs(new DelegateReference<>()));
  }

  private static class DelegateReference<K, V> extends TypeReference<Map<K, Templatable<V>>> {

  }
}
