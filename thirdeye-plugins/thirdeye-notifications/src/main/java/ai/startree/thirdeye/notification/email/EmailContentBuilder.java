/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.Constants.SubjectType;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

public class EmailContentBuilder {

  public static final String DEFAULT_EMAIL_TEMPLATE = "metric-anomalies";
  private static final String BASE_PACKAGE_PATH = "/ai/startree/thirdeye/detection/detector";
  private static final String CHARSET = "UTF-8";
  private static final Map<String, String> TEMPLATE_MAP = ImmutableMap.<String, String>builder()
      .put(DEFAULT_EMAIL_TEMPLATE, "metric-anomalies-template.ftl")
      .put("entity-groupkey", "entity-groupkey-anomaly-report.ftl")
      .put("hierarchical-anomalies",
          "hierarchical-anomalies-email-template.ftl")
      .build();

  /**
   * Generate subject based on configuration.
   */
  public String makeSubject(final SubjectType subjectType,
      final Object metrics,
      final Object datasets,
      final String subscriptionGroupName) {
    final String baseSubject = "Thirdeye Alert : " + subscriptionGroupName;

    switch (subjectType) {
      case ALERT:
        return baseSubject;

      case METRICS:
        return baseSubject + " - " + metrics;

      case DATASETS:
        return baseSubject + " - " + datasets;

      default:
        throw new IllegalArgumentException(String.format("Unknown type '%s'", subjectType));
    }
  }

  public String buildHtml(final String templateFile, final Map<String, Object> templateValues) {
    final String templateName = TEMPLATE_MAP.get(templateFile);

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (final Writer out = new OutputStreamWriter(baos, CHARSET)) {
      final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
      freemarkerConfig.setClassForTemplateLoading(getClass(),
          BASE_PACKAGE_PATH);
      freemarkerConfig.setDefaultEncoding(CHARSET);
      freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

      final Template template = freemarkerConfig.getTemplate(templateName);
      template.process(templateValues, out);

      return baos.toString(CHARSET);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
