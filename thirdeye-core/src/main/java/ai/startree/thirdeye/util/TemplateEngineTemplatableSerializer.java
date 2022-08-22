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
package ai.startree.thirdeye.util;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This serializer can be used to apply template properties on {@link Templatable} fields.
 * Do not use for API or persistence.
 */
public class TemplateEngineTemplatableSerializer extends JsonSerializer<Templatable> {

  private final Map<String, Object> valuesMap;

  public TemplateEngineTemplatableSerializer(final Map<String, Object> valuesMap) {
    this.valuesMap = valuesMap;
  }

  @Override
  public void serialize(final Templatable templatable, final JsonGenerator jsonGenerator,
      final SerializerProvider serializerProvider) throws IOException {
    final String templatedValue = templatable.templatedValue();
    if (templatedValue != null) {
      final String property = templatedValue.substring(2, templatedValue.length() - 1);
      checkArgument(valuesMap.containsKey(property), "Property not provided for templatable value: %s", property);
      final @Nullable Object value = valuesMap.get(property);
      jsonGenerator.writeObject(Templatable.of(value));
    } else {
      // cannot call writeObject --> this would create an infinite recursive loop
      jsonGenerator.writeStartObject();
      jsonGenerator.writeObjectField(Templatable.VALUE_FIELD_STRING,  templatable.value());
      jsonGenerator.writeEndObject();
    }
  }
}
