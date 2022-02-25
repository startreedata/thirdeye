/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.Constants.SubjectType;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.EmailRecipientsApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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

  String buildHtml(final String templateFile, final Map<String, Object> templateValues) {
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

  public EmailEntityApi buildEmailEntityApi(final NotificationPayloadApi api) {
    final Map<String, Object> templateData = constructTemplateData(api);
    return buildEmailEntityApi(api.getSubscriptionGroup(),
        DEFAULT_EMAIL_TEMPLATE,
        templateData,
        api.getEmailRecipients());
  }

  private EmailEntityApi buildEmailEntityApi(final SubscriptionGroupApi subscriptionGroup,
      final String templateKey,
      final Map<String, Object> templateData,
      final EmailRecipientsApi recipients) {
    requireNonNull(recipients.getTo(), "to field in email scheme is null");
    checkArgument(recipients.getTo().size() > 0, "'to' field in email scheme is empty");

    final String htmlText = buildHtml(templateKey, templateData);

    final String subject = makeSubject(SubjectType.ALERT,
        templateData.get("metrics"),
        templateData.get("datasets"),
        subscriptionGroup.getName());

    return new EmailEntityApi()
        .setSubject(subject)
        .setHtmlContent(htmlText)
        .setRecipients(recipients)
        .setFrom(recipients.getFrom());
  }

  public Map<String, Object> constructTemplateData(
      final NotificationPayloadApi api) {
    final Map<String, Object> templateData = new HashMap<>();
    templateData.put("anomalyCount", api.getAnomalyReports().size());

    final List<String> anomalyIds = api.getAnomalyReports().stream()
        .map(AnomalyReportApi::getAnomaly)
        .map(AnomalyApi::getId)
        .filter(Objects::nonNull)
        .map(Object::toString)
        .collect(Collectors.toList());
    templateData.put("anomalyIds", Joiner.on(",").join(anomalyIds));

    final String commaSeparatedAnomalyIds = api.getAnomalyReports()
        .stream()
        .map(AnomalyReportApi::getAnomaly)
        .map(AnomalyApi::getId)
        .map(Objects::toString)
        .collect(Collectors.joining(","));
    templateData.put("anomalyIds", commaSeparatedAnomalyIds);

    final NotificationReportApi report = api.getReport();
    templateData.put("startTime", report.getStartTime());
    templateData.put("endTime", report.getEndTime());
    templateData.put("timeZone", report.getTimeZone());
    templateData.put("notifiedCount", report.getNotifiedCount());
    templateData.put("feedbackCount", report.getFeedbackCount());
    templateData.put("trueAlertCount", report.getTrueAlertCount());
    templateData.put("falseAlertCount", report.getFalseAlertCount());
    templateData.put("newTrendCount", report.getNewTrendCount());
    templateData.put("alertConfigName", report.getAlertConfigName());
    templateData.put("includeSummary", report.getIncludeSummary());
    templateData.put("reportGenerationTimeMillis", report.getReportGenerationTimeMillis());
    templateData.put("precision", report.getPrecision());
    templateData.put("recall", report.getRecall());
    templateData.put("falseNegative", report.getFalseNegative());
    templateData.put("referenceLinks", report.getReferenceLinks());
    templateData.put("dashboardHost", report.getDashboardHost());
    templateData.put("holidays", report.getRelatedEvents());

    templateData.put("anomalyDetails", new HashMap<>());
    templateData.put("detectionToAnomalyDetailsMap", new HashMap<>());
    templateData.put("metricToAnomalyDetailsMap", new HashMap<>());
    templateData.put("functionToId", new HashMap<>());

    // TODO spyne populate dataset info
    templateData.put("datasetsCount", 0);
    templateData.put("datasets", "");

    // TODO spyne populate metrics map. check how this is being used
    templateData.put("metricsMap", new TreeMap<>());

    final Set<String> metricNames = api.getAnomalyReports()
        .stream()
        .map(AnomalyReportApi::getAnomaly)
        .map(AnomalyApi::getMetric)
        .filter(Objects::nonNull)
        .map(MetricApi::getName)
        .collect(Collectors.toSet());
    templateData.put("metricsCount", metricNames.size());
    templateData.put("metrics", StringUtils.join(metricNames, ","));

    // TODO spyne this is used only if "cid" is present. used in screenshots. can handle later
    templateData.put("anomalyDetails", new HashMap<>());

    // TODO spyne remove links to emailTemplateData
    templateData.put("detectionToAnomalyDetailsMap",
        api.getEmailTemplateData().get("detectionToAnomalyDetailsMap"));
    templateData.put("metricToAnomalyDetailsMap",
        api.getEmailTemplateData().get("metricToAnomalyDetailsMap"));

    // TODO spyne used to add nav to alerts. fix
    templateData.put("functionToId", new HashMap<>());

    return templateData;
  }
}
