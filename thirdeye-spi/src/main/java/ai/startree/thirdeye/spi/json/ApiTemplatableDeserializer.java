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

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;

/**
 * For wrapped generic type inference at runtime,
 * see https://stackoverflow.com/questions/36159677/how-to-create-a-custom-deserializer-in-jackson-for-a-generic-type
 */
class ApiTemplatableDeserializer extends JsonDeserializer<Templatable<?>>
    implements ContextualDeserializer {

  private JavaType valueType;

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
      throws JsonMappingException {
    checkArgument(property != null,
        "Cannot deserialize Templatable object without its generic type. Attempt to use Templatable as root object?");
    final JavaType wrapperType = property.getType();
    final JavaType valueType = wrapperType.containedType(0);
    final ApiTemplatableDeserializer deserializer = new ApiTemplatableDeserializer();
    deserializer.valueType = valueType;
    return deserializer;
  }

  @Override
  public Templatable<?> deserialize(final JsonParser jsonParser,
      final DeserializationContext context)
      throws IOException, JsonProcessingException {
    // case value is a variable in format ${VARIABLE_NAME}
    final String textValue = jsonParser.getText();
    if (textValue.startsWith("${")) {
      final String stringValue = context.readValue(jsonParser, String.class);
      return new Templatable<>().setTemplatedValue(stringValue);
    }

    // case value is of type T in Templatable<T>
    try {
      final Templatable<?> templatable = new Templatable<>();
      templatable.setValue(context.readValue(jsonParser, valueType));
      return templatable;
    } catch (MismatchedInputException e) {
      throw new IllegalArgumentException(
          String.format(
              "Invalid value: %s.Templatable field is of type %s. "
                  + "Value should be in templated format \"${VAR_NAME}\" or in the object field map format {\"key\": \"value\"}",
              textValue,
              valueType
          ), e);
    }
  }
}
