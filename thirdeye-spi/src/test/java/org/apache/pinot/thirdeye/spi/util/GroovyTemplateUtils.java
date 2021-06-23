package org.apache.pinot.thirdeye.spi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.text.SimpleTemplateEngine;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.pinot.thirdeye.spi.api.v2.AlertEvaluationPlanApi;

public class GroovyTemplateUtils {

  private static final SimpleTemplateEngine GROOVY_TEMPLATE_ENGINE = new SimpleTemplateEngine();
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    GROOVY_TEMPLATE_ENGINE.setEscapeBackslash(true);
  }

  public static String renderTemplate(String template, Map<String, Object> newContext)
      throws IOException, ClassNotFoundException {
    Map<String, Object> contextMap = getDefaultContextMap();
    contextMap.putAll(newContext);
    return GROOVY_TEMPLATE_ENGINE.createTemplate(template).make(contextMap).toString();
  }

  /**
   * Construct default template context:
   * today : today's date in format `yyyy-MM-dd`, example value: '2020-05-06'
   * yesterday : yesterday's date in format `yyyy-MM-dd`, example value: '2020-05-06'
   */
  public static Map<String, Object> getDefaultContextMap() {
    Map<String, Object> defaultContextMap = new HashMap<>();
    Instant now = Instant.now();
    defaultContextMap.put("today", DATE_FORMAT.format(new Date(now.toEpochMilli())));
    defaultContextMap.put("yesterday",
        DATE_FORMAT.format(new Date(now.minus(1, ChronoUnit.DAYS).toEpochMilli())));
    return defaultContextMap;
  }

  public static Map<String, Object> getTemplateContext(List<String> values) {
    if (values == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> context = new HashMap<>();
    for (String value : values) {
      String[] splits = value.split("=", 2);
      if (splits.length > 1) {
        context.put(splits[0], splits[1]);
      }
    }
    return context;
  }

  public static String renderTemplate(String template)
      throws IOException, ClassNotFoundException {
    return renderTemplate(template, Collections.emptyMap());
  }

  /**
   * TODO move this method to thirdeye-core along with the groovy dependency
   *
   * @param template
   * @param context
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static AlertEvaluationPlanApi applyContextToTemplate(String template,
      Map<String, Object> context) throws IOException, ClassNotFoundException {
    return new ObjectMapper().readValue(
        renderTemplate(template, context), AlertEvaluationPlanApi.class);
  }
}
