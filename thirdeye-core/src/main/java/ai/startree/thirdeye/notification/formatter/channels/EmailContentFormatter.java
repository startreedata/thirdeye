/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.formatter.channels;

import ai.startree.thirdeye.notification.NotificationContext;
import ai.startree.thirdeye.notification.content.BaseNotificationContent;
import ai.startree.thirdeye.notification.content.NotificationContent;
import ai.startree.thirdeye.notification.content.templates.EntityGroupKeyContent;
import ai.startree.thirdeye.notification.content.templates.HierarchicalAnomaliesContent;
import ai.startree.thirdeye.notification.content.templates.MetricAnomaliesContent;
import ai.startree.thirdeye.spi.Constants.SubjectType;
import ai.startree.thirdeye.spi.api.EmailEntityApi;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * This class formats the content for email alerts.
 */
public class EmailContentFormatter {

  public static final Map<String, String> TEMPLATE_MAP = ImmutableMap.<String, String>builder()
      .put(MetricAnomaliesContent.class.getSimpleName(), "metric-anomalies-template.ftl")
      .put(EntityGroupKeyContent.class.getSimpleName(), "entity-groupkey-anomaly-report.ftl")
      .put(HierarchicalAnomaliesContent.class.getSimpleName(),
          "hierarchical-anomalies-email-template.ftl")
      .build();

  protected static final String PROP_SUBJECT_STYLE = "subject";

  private static final String BASE_PACKAGE_PATH = "/ai/startree/thirdeye/detection/detector";
  private static final String CHARSET = "UTF-8";

  /**
   * Plug the appropriate subject style based on configuration
   */
  SubjectType getSubjectType(final Properties alertSchemeClientConfigs,
      final SubscriptionGroupDTO subsConfig) {
    final SubjectType subjectType;
    if (alertSchemeClientConfigs != null && alertSchemeClientConfigs
        .containsKey(PROP_SUBJECT_STYLE)) {
      subjectType = SubjectType
          .valueOf(alertSchemeClientConfigs.get(PROP_SUBJECT_STYLE).toString());
    } else {
      // To support the legacy email subject configuration
      subjectType = subsConfig.getSubjectType();
    }

    return subjectType;
  }

  public EmailEntityApi getEmailEntity(
      final NotificationContext notificationContext,
      final NotificationContent content,
      final SubscriptionGroupDTO subsConfig,
      final Collection<AnomalyResult> anomalies) {
    final Map<String, Object> templateData = content.format(anomalies, subsConfig);
    templateData.put("dashboardHost", notificationContext.getUiPublicUrl());

    final String templateName = TEMPLATE_MAP.get(content.getTemplate());
    final String htmlText = buildHtml(templateName, templateData);
    final SubjectType subjectType = getSubjectType(notificationContext.getProperties(), subsConfig);
    final String subject = BaseNotificationContent.makeSubject(subjectType,
        subsConfig,
        templateData);

    return new EmailEntityApi()
        .setSnapshotPath(content.getSnaphotPath())
        .setSubject(subject)
        .setHtmlContent(htmlText);
  }

  public String buildHtml(final String templateName, final Map<String, Object> templateValues) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (final Writer out = new OutputStreamWriter(baos, CHARSET)) {
      final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
      freemarkerConfig.setClassForTemplateLoading(getClass(), BASE_PACKAGE_PATH);
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
