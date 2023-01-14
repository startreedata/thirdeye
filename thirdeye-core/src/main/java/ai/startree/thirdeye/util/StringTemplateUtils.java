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
package ai.startree.thirdeye.util;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_TEMPLATE_MISSING_PROPERTY;

import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.text.StringSubstitutor;

public class StringTemplateUtils {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public static String renderTemplate(final String template, final Map<String, Object> newContext) {
    final Map<String, Object> contextMap = getDefaultContextMap();
    contextMap.putAll(newContext);

    final StringSubstitutor sub = new StringSubstitutor(contextMap)
        .setDisableSubstitutionInValues(true)
        .setEnableUndefinedVariableException(true);
    try {
      return sub.replace(template);
    } catch (final IllegalArgumentException e) {
      throw new ThirdEyeException(ERR_TEMPLATE_MISSING_PROPERTY, e);
    }
  }

  /**
   * Construct default template context:
   * today : today's date in format `yyyy-MM-dd`, example value: '2020-05-06'
   * yesterday : yesterday's date in format `yyyy-MM-dd`, example value: '2020-05-06'
   */
  public static Map<String, Object> getDefaultContextMap() {
    final Map<String, Object> defaultContextMap = new HashMap<>();
    final Instant now = Instant.now();
    defaultContextMap.put("today", DATE_FORMAT.format(new Date(now.toEpochMilli())));
    defaultContextMap.put("yesterday",
        DATE_FORMAT.format(new Date(now.minus(1, ChronoUnit.DAYS).toEpochMilli())));
    return defaultContextMap;
  }

  @SuppressWarnings("unchecked")
  public static <T> T applyContext(final T template,
      final Map<String, Object> valuesMap)
      throws IOException, ClassNotFoundException {

    final Module module = new SimpleModule().addSerializer(Templatable.class,
        new TemplateEngineTemplatableSerializer(valuesMap));
    final ObjectMapper objectMapper = new ObjectMapper().registerModule(module);

    final String jsonString = objectMapper.writeValueAsString(template);
    return (T) objectMapper.readValue(renderTemplate(jsonString, valuesMap), template.getClass());
  }
}
