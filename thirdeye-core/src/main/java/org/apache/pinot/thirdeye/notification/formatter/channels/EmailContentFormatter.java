/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.notification.formatter.channels;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.HierarchicalAnomaliesContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class formats the content for email alerts.
 */
public class EmailContentFormatter extends AlertContentFormatter {

  private static final Logger LOG = LoggerFactory.getLogger(EmailContentFormatter.class);
  private static final String BASE_PACKAGE_PATH = "/org/apache/pinot/thirdeye/detection/detector";
  private static final String CHARSET = "UTF-8";

  private static final Map<String, String> TEMPLATE_MAP = ImmutableMap.<String, String>builder()
      .put(MetricAnomaliesContent.class.getSimpleName(), "metric-anomalies-template.ftl")
      .put(EntityGroupKeyContent.class.getSimpleName(), "entity-groupkey-anomaly-report.ftl")
      .put(HierarchicalAnomaliesContent.class.getSimpleName(),
          "hierarchical-anomalies-email-template.ftl")
      .build();

  public EmailContentFormatter(final Properties emailClientConfig,
      final BaseNotificationContent content,
      final ThirdEyeCoordinatorConfiguration teConfig,
      final SubscriptionGroupDTO subscriptionGroup) {
    super(emailClientConfig, content, teConfig, subscriptionGroup);
  }

  public EmailEntity getEmailEntity(final Collection<AnomalyResult> anomalies) {
    final Map<String, Object> templateData = notificationContent.format(anomalies, subsConfig);
    templateData.put("dashboardHost", teConfig.getDashboardHost());
    return buildEmailEntity(TEMPLATE_MAP.get(notificationContent.getTemplate()),
        templateData);
  }

  public String getEmailHtml(final Collection<AnomalyResult> anomalies) {
    final Map<String, Object> templateData = notificationContent.format(anomalies, subsConfig);
    templateData.put("dashboardHost", teConfig.getDashboardHost());
    return buildHtml(TEMPLATE_MAP.get(notificationContent.getTemplate()),
        templateData);
  }

  /**
   * Apply the parameter map to given email template, and format it as EmailEntity
   */
  private EmailEntity buildEmailEntity(final String templateName,
      final Map<String, Object> templateValues) {
    final HtmlEmail htmlEmail = new HtmlEmail();
    String cid = "";
    try {
      if (StringUtils.isNotBlank(notificationContent.getSnaphotPath())) {
        cid = htmlEmail.embed(new File(notificationContent.getSnaphotPath()));
      }
    } catch (final Exception e) {
      LOG.error("Exception while embedding screenshot for anomaly", e);
    }
    templateValues.put("cid", cid);

    final String htmlText = buildHtml(templateName, templateValues);
    return buildEmailEntity(templateValues, htmlEmail, htmlText);
  }

  private String buildHtml(final String templateName, final Map<String, Object> templateValues) {
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

  private EmailEntity buildEmailEntity(final Map<String, Object> templateValues,
      final HtmlEmail email, final String htmlEmail) {
    try {
      final EmailEntity emailEntity = new EmailEntity();
      final String subject = BaseNotificationContent
          .makeSubject(super.getSubjectType(alertClientConfig), subsConfig, templateValues);
      emailEntity.setSubject(subject);
      email.setHtmlMsg(htmlEmail);
      emailEntity.setContent(email);
      return emailEntity;
    } catch (final EmailException e) {
      throw new RuntimeException(e);
    }
  }
}
