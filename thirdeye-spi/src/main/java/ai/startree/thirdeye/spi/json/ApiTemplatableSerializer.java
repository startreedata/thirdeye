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
package ai.startree.thirdeye.spi.json;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * This Serializer is used to simulate a union type for Templatable fields. At the json level,
 * Templatable\<T\> behaves like Union[String, T].
 * For field1 of type Templatable\<T\>. With this Deserializer, the json form is:
 * "field1": "${var}"  (templated form) or "field1": T_json with T_json the json representation of
 * T.
 *
 * You should not use this class directly. See {@link ThirdEyeSerialization}.
 *
 * For wrapped generic type inference at runtime,
 * see https://stackoverflow.com/questions/36159677/how-to-create-a-custom-deserializer-in-jackson-for-a-generic-type
 */
class ApiTemplatableSerializer extends JsonSerializer<Templatable> {

  @Override
  public void serialize(final Templatable templatable, final JsonGenerator jsonGenerator,
      final SerializerProvider serializerProvider) throws IOException {
    if (templatable.templatedValue() != null) {
      jsonGenerator.writeString(templatable.templatedValue());
    } else {
      serializerProvider.defaultSerializeValue(templatable.value(), jsonGenerator);
    }
  }
}
