/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.text.SimpleTemplateEngine;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class StringTemplateUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final SimpleTemplateEngine GROOVY_TEMPLATE_ENGINE = new SimpleTemplateEngine();
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    GROOVY_TEMPLATE_ENGINE.setEscapeBackslash(true);
  }

  public static String renderTemplate(final String template, final Map<String, Object> newContext)
      throws IOException, ClassNotFoundException {
    final Map<String, Object> contextMap = getDefaultContextMap();
    contextMap.putAll(newContext);
    return GROOVY_TEMPLATE_ENGINE.createTemplate(template).make(contextMap).toString();
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
    final String jsonString = OBJECT_MAPPER.writeValueAsString(template);
    return (T) OBJECT_MAPPER.readValue(renderTemplate(jsonString, valuesMap), template.getClass());
  }
}
