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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TEMPLATE_MISSING_PROPERTY;

import ai.startree.thirdeye.spi.ThirdEyeException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

/**
 * This serializer can be used to apply template properties on String fields.
 * Do not use for API or persistence.
 */
public class TemplateEngineStringSerializer extends JsonSerializer<String> {

  private final StringSubstitutor sub;

  public TemplateEngineStringSerializer(final Map<String, Object> valuesMap) {
    this.sub = new StringSubstitutor(valuesMap).setDisableSubstitutionInValues(true)
        .setEnableUndefinedVariableException(true);
  }

  @Override
  public void serialize(final String stringField, final JsonGenerator jsonGenerator,
      final SerializerProvider serializerProvider) throws IOException {
    final String replace;
    try {
      replace = sub.replace(stringField);
    } catch (final IllegalArgumentException e) {
      final String message = e.getMessage();
      throw new ThirdEyeException(ERR_TEMPLATE_MISSING_PROPERTY, message);
  }
    jsonGenerator.writeString(replace);
  }
}
