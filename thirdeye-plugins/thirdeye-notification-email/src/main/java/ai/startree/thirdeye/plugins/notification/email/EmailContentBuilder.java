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
package ai.startree.thirdeye.plugins.notification.email;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.Constants.SubjectType;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyReportApi;
import ai.startree.thirdeye.spi.api.AnomalyReportDataApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.NotificationReportApi;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
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

  public EmailContent build(final NotificationPayloadApi api) {
    final Map<String, Object> templateData = constructTemplateData(api);
    final String htmlText = buildHtml(DEFAULT_EMAIL_TEMPLATE, templateData);

    final String subject = makeSubject(SubjectType.ALERT,
        templateData.get("metrics"),
        templateData.get("datasets"),
        api.getSubscriptionGroup().getName());

    return new EmailContent()
        .setSubject(subject)
        .setHtmlBody(htmlText);
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

    templateData.put("detectionToAnomalyDetailsMap",
        buildDetectionToAnomalyDetailsMap(api.getAnomalyReports()));
    templateData.put("metricToAnomalyDetailsMap",
        buildMetricToAnomalyDetailsMap(api.getAnomalyReports()));

    // TODO spyne used to add nav to alerts. fix
    templateData.put("functionToId", new HashMap<>());

    return templateData;
  }

  private Map<String, Collection<AnomalyReportDataApi>> buildDetectionToAnomalyDetailsMap(
      final List<AnomalyReportApi> anomalyReports) {
    final Multimap<String, AnomalyReportDataApi> map = ArrayListMultimap.create();
    for (AnomalyReportApi anomalyReportApi : anomalyReports) {
      final AnomalyReportDataApi data = anomalyReportApi.getData();
      optional(anomalyReportApi.getAnomaly())
          .map(AnomalyApi::getEnumerationItem)
          .map(EnumerationItemApi::getName)
          .ifPresent(name -> data.setDimensions(List.of(name)));
      map.put(data.getFunction(), data);
    }
    return map.asMap();
  }

  private Map<String, Collection<AnomalyReportDataApi>> buildMetricToAnomalyDetailsMap(
      final List<AnomalyReportApi> anomalyReports) {
    final Multimap<String, AnomalyReportDataApi> map = ArrayListMultimap.create();
    for (AnomalyReportApi anomalyReportApi : anomalyReports) {
      final String metricName = optional(anomalyReportApi.getAnomaly().getMetric())
          .map(MetricApi::getName)
          .orElse("UNKNOWN");
      map.put(metricName, anomalyReportApi.getData());
    }
    return map.asMap();
  }
}
