/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.notification.content.templates;

import static org.apache.pinot.thirdeye.datalayer.DaoTestUtils.getTestDatasetConfig;
import static org.apache.pinot.thirdeye.datalayer.DaoTestUtils.getTestMetricConfig;
import static org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration.SMTP_HOST_KEY;
import static org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration.SMTP_PORT_KEY;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.anomaly.AnomalyType;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorConfiguration;
import org.apache.pinot.thirdeye.anomaly.task.TaskDriverConfiguration;
import org.apache.pinot.thirdeye.anomalydetection.context.AnomalyResult;
import org.apache.pinot.thirdeye.common.restclient.MockThirdEyeRcaRestClient;
import org.apache.pinot.thirdeye.common.restclient.ThirdEyeRcaRestClient;
import org.apache.pinot.thirdeye.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.constant.AnomalyResultSource;
import org.apache.pinot.thirdeye.datalayer.DaoTestUtils;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.DAOTestBase;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.detection.ConfigUtils;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.DefaultDataProvider;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.detection.cache.builder.AnomaliesCacheBuilder;
import org.apache.pinot.thirdeye.detection.cache.builder.TimeSeriesCacheBuilder;
import org.apache.pinot.thirdeye.detection.components.ThresholdRuleDetector;
import org.apache.pinot.thirdeye.notification.ContentFormatterUtils;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.formatter.channels.EmailContentFormatter;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestMetricAnomaliesContent {

  private static final String TEST = "test";
  private final int id = 0;
  private final String dashboardHost = "http://localhost:8080/dashboard";
  private final String detectionConfigFile = "/sample-detection-config.yml";
  private final ObjectMapper mapper = new ObjectMapper();
  private DAOTestBase testDAOProvider;
  private AlertManager detectionConfigDAO;
  private MergedAnomalyResultManager mergedAnomalyResultDAO;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private EventManager eventDAO;
  private EvaluationManager evaluationDAO;
  private DataProvider provider;

  @BeforeMethod
  public void beforeMethod() {
    testDAOProvider = DAOTestBase.getInstance();
    DAORegistry daoRegistry = DAORegistry.getInstance();
    detectionConfigDAO = daoRegistry.getDetectionConfigManager();
    mergedAnomalyResultDAO = daoRegistry.getMergedAnomalyResultDAO();
    metricDAO = daoRegistry.getMetricConfigDAO();
    datasetDAO = daoRegistry.getDatasetConfigDAO();
    eventDAO = daoRegistry.getEventDAO();
    evaluationDAO = daoRegistry.getEvaluationManager();

    AggregationLoader aggregationLoader =
        new DefaultAggregationLoader(metricDAO, datasetDAO, DeprecatedInjectorUtil
            .getInstance(ThirdEyeCacheRegistry.class).getDataSourceCache(),
            DeprecatedInjectorUtil.getInstance(ThirdEyeCacheRegistry.class)
                .getDatasetMaxDataTimeCache());

    provider = new DefaultDataProvider(metricDAO,
        datasetDAO,
        eventDAO,
        evaluationDAO,
        aggregationLoader,
        TimeSeriesCacheBuilder.getInstance(),
        AnomaliesCacheBuilder.getInstance());
  }

  @AfterMethod(alwaysRun = true)
  void afterMethod() {
    testDAOProvider.cleanup();
  }

  @Test
  public void testGetEmailEntity() throws Exception {
    DetectionRegistry.registerComponent(ThresholdRuleDetector.class.getName(), "THRESHOLD");

    DateTimeZone dateTimeZone = DateTimeZone.forID("America/Los_Angeles");
    ThirdEyeWorkerConfiguration thirdeyeAnomalyConfig = new ThirdEyeWorkerConfiguration();
    thirdeyeAnomalyConfig.setId(id);
    thirdeyeAnomalyConfig.setDashboardHost(dashboardHost);
    MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
    monitorConfiguration.setMonitorFrequency(new TimeGranularity(3, TimeUnit.SECONDS));
    thirdeyeAnomalyConfig.setMonitorConfiguration(monitorConfiguration);

    final TaskDriverConfiguration taskDriverConfiguration = new TaskDriverConfiguration()
        .setNoTaskDelay(Duration.ofMillis(1000))
        .setRandomDelayCap(Duration.ofMillis(200))
        .setTaskFailureDelay(Duration.ofMillis(500))
        .setMaxParallelTasks(2);

    thirdeyeAnomalyConfig.setTaskDriverConfiguration(taskDriverConfiguration);
    thirdeyeAnomalyConfig.setRootDir(System.getProperty("dw.rootDir", "NOT_SET(dw.rootDir)"));
    Map<String, Map<String, Object>> alerters = new HashMap<>();
    Map<String, Object> smtpProps = new HashMap<>();
    smtpProps.put(SMTP_HOST_KEY, "host");
    smtpProps.put(SMTP_PORT_KEY, "9000");
    alerters.put("smtpConfiguration", smtpProps);
    thirdeyeAnomalyConfig.setAlerterConfiguration(alerters);

    // create test dataset config
    datasetDAO.save(getTestDatasetConfig("test-collection"));
    metricDAO.save(getTestMetricConfig("test-collection", "cost", null));

    List<AnomalyResult> anomalies = new ArrayList<>();
    AlertDTO alertDTO = DaoTestUtils.getTestDetectionConfig(provider, detectionConfigFile);
    detectionConfigDAO.save(alertDTO);

    MergedAnomalyResultDTO anomaly = DaoTestUtils.getTestMergedAnomalyResult(
        new DateTime(2020, 1, 6, 10, 0, dateTimeZone).getMillis(),
        new DateTime(2020, 1, 6, 13, 0, dateTimeZone).getMillis(),
        TEST, TEST, 0.1, 1l, new DateTime(2020, 1, 6, 10, 0, dateTimeZone).getMillis());
    anomaly.setDetectionConfigId(alertDTO.getId());
    anomaly.setAvgCurrentVal(1.1);
    anomaly.setAvgBaselineVal(1.0);
    anomaly.setMetricUrn("thirdeye:metric:1");
    mergedAnomalyResultDAO.save(anomaly);
    anomalies.add(anomaly);

    anomaly = DaoTestUtils.getTestMergedAnomalyResult(
        new DateTime(2020, 1, 7, 10, 0, dateTimeZone).getMillis(),
        new DateTime(2020, 1, 7, 17, 0, dateTimeZone).getMillis(),
        TEST, TEST, 0.1, 1l, new DateTime(2020, 1, 6, 10, 0, dateTimeZone).getMillis());
    anomaly.setDetectionConfigId(alertDTO.getId());
    anomaly.setAvgCurrentVal(0.9);
    anomaly.setAvgBaselineVal(Double.NaN);
    anomaly.setMetricUrn("thirdeye:metric:2");
    mergedAnomalyResultDAO.save(anomaly);
    anomalies.add(anomaly);

    anomaly = DaoTestUtils.getTestMergedAnomalyResult(
        new DateTime(2020, 1, 1, 10, 0, dateTimeZone).getMillis(),
        new DateTime(2020, 1, 7, 17, 0, dateTimeZone).getMillis(),
        TEST, TEST, 0.1, 1l, new DateTime(2020, 1, 7, 17, 1, dateTimeZone).getMillis());
    anomaly.setDetectionConfigId(alertDTO.getId());
    anomaly.setType(AnomalyType.DATA_SLA);
    anomaly.setAnomalyResultSource(AnomalyResultSource.DATA_QUALITY_DETECTION);
    anomaly.setMetricUrn("thirdeye:metric:3");
    Map<String, String> props = new HashMap<>();
    props.put("sla", "3_DAYS");
    anomaly.setProperties(props);
    mergedAnomalyResultDAO.save(anomaly);
    anomalies.add(anomaly);

    anomaly = DaoTestUtils.getTestMergedAnomalyResult(
        new DateTime(2020, 1, 1, 0, 0, dateTimeZone).getMillis(),
        new DateTime(2020, 1, 7, 5, 5, dateTimeZone).getMillis(),
        TEST, TEST, 0.1, 1l, new DateTime(2020, 1, 7, 5, 6, dateTimeZone).getMillis());
    anomaly.setDetectionConfigId(alertDTO.getId());
    anomaly.setType(AnomalyType.DATA_SLA);
    anomaly.setAnomalyResultSource(AnomalyResultSource.DATA_QUALITY_DETECTION);
    anomaly.setMetricUrn("thirdeye:metric:3");
    props = new HashMap<>();
    props.put("datasetLastRefreshTime",
        "" + new DateTime(2020, 1, 4, 0, 0, dateTimeZone).getMillis());
    props.put("sla", "2_DAYS");
    anomaly.setProperties(props);
    mergedAnomalyResultDAO.save(anomaly);
    anomalies.add(anomaly);

    anomaly = DaoTestUtils.getTestMergedAnomalyResult(
        new DateTime(2020, 1, 1, 10, 5, dateTimeZone).getMillis(),
        new DateTime(2020, 1, 1, 15, 0, dateTimeZone).getMillis(),
        TEST, TEST, 0.1, 1l, new DateTime(2020, 1, 1, 16, 0, dateTimeZone).getMillis());
    anomaly.setDetectionConfigId(alertDTO.getId());
    anomaly.setType(AnomalyType.DATA_SLA);
    anomaly.setAnomalyResultSource(AnomalyResultSource.DATA_QUALITY_DETECTION);
    anomaly.setMetricUrn("thirdeye:metric:3");
    props = new HashMap<>();
    props.put("datasetLastRefreshTime",
        "" + new DateTime(2020, 1, 1, 10, 5, dateTimeZone).getMillis());
    props.put("sla", "3_HOURS");
    anomaly.setProperties(props);
    mergedAnomalyResultDAO.save(anomaly);
    anomalies.add(anomaly);

    MetricConfigDTO metric = new MetricConfigDTO();
    metric.setName(TEST);
    metric.setDataset(TEST);
    metric.setAlias(TEST + "::" + TEST);
    metricDAO.save(metric);

    Map<String, Object> expectedResponse = new HashMap<>();
    ThirdEyeRcaRestClient rcaClient = MockThirdEyeRcaRestClient.setupMockClient(expectedResponse);
    MetricAnomaliesContent metricAnomaliesContent = new MetricAnomaliesContent(rcaClient);
    EmailContentFormatter
        contentFormatter = new EmailContentFormatter(new Properties(), metricAnomaliesContent,
        thirdeyeAnomalyConfig, DaoTestUtils.getTestNotificationConfig("Test Config"));
    EmailEntity emailEntity = contentFormatter.getEmailEntity(anomalies);

    String htmlPath = ClassLoader.getSystemResource("test-metric-anomalies-template.html")
        .getPath();
    Assert.assertEquals(
        ContentFormatterUtils.getEmailHtml(emailEntity).replaceAll("\\s", ""),
        ContentFormatterUtils.getHtmlContent(htmlPath).replaceAll("\\s", ""));
  }

  @Test
  public void testRCAHighlights() throws TemplateException, IOException {
    Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    cfg.setClassForTemplateLoading(TestMetricAnomaliesContent.class,
        "/org/apache/pinot/thirdeye/detector/");
    Template template = cfg.getTemplate("metric-anomalies-template.ftl");
    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("anomalyCount", 1);
    model.put("metricsMap", new HashMap<>());
    model.put("startTime", 0);
    model.put("endTime", 10);
    model.put("timeZone", "UTC");
    model.put("dashboardHost", dashboardHost);
    model.put("anomalyIds", "");
    model.put("metricToAnomalyDetailsMap", new HashMap<>());
    model.put("alertConfigName", "test_alert");

    String cubeResponsePath = ClassLoader
        .getSystemResource("test-email-rca-highlights-cube-algo-response.json").getPath();
    Map<String, Object> cubeResults = mapper.readValue(new File(
        cubeResponsePath), new TypeReference<Map<String, Object>>() {
    });
    model.put("cubeDimensions",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("dimensions"));
    model.put("cubeResponseRows",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("responseRows"));

    Writer out = new StringWriter();
    template.process(model, out);

    String rcaResultRendered = ClassLoader
        .getSystemResource("test-email-rca-highlights-cube-algo-response-rendered.html").getPath();
    Assert.assertEquals(out.toString(), ContentFormatterUtils.getHtmlContent(rcaResultRendered));
  }

  @Test
  public void testRCAHighlightsWithErrorRCAResponse() throws TemplateException, IOException {
    Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    cfg.setClassForTemplateLoading(TestMetricAnomaliesContent.class,
        "/org/apache/pinot/thirdeye/detector/");
    Template template = cfg.getTemplate("metric-anomalies-template.ftl");
    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("anomalyCount", 1);
    model.put("metricsMap", new HashMap<>());
    model.put("startTime", 0);
    model.put("endTime", 10);
    model.put("timeZone", "UTC");
    model.put("dashboardHost", dashboardHost);
    model.put("anomalyIds", "");
    model.put("metricToAnomalyDetailsMap", new HashMap<>());
    model.put("alertConfigName", "test_alert");

    Writer out = new StringWriter();

    // email template should not break even if cubeResults are null or empty
    Map<String, Object> rootCauseHighlights = new HashMap<>();
    rootCauseHighlights.put("cubeResult", null);
    model.put("rootCauseHighlights", rootCauseHighlights);
    template.process(model, out);
    rootCauseHighlights.put("cubeResult", new HashMap<>());
    model.put("rootCauseHighlights", rootCauseHighlights);
    template.process(model, out);

    // email template should not break even if dimension field under cubeResults are null or empty
    String cubeResponsePath = ClassLoader
        .getSystemResource("test-email-rca-highlights-cube-algo-response.json").getPath();
    Map<String, Object> cubeResults = Collections.unmodifiableMap(mapper.readValue(
        new File(cubeResponsePath), new TypeReference<Map<String, Object>>() {
        }));
    model.put("cubeDimensions", null);
    model.put("cubeResponseRows",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("responseRows"));
    template.process(model, out);
    model.put("cubeDimensions", new ArrayList<>());
    model.put("cubeResponseRows",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("responseRows"));
    template.process(model, out);

    // email template should not break even if dimension field under cubeResults are null or empty
    model.put("cubeDimensions",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("dimensions"));
    model.put("cubeResponseRows", null);
    template.process(model, out);
    model.put("cubeDimensions",
        ConfigUtils.getMap(cubeResults.get("cubeResults")).get("dimensions"));
    model.put("cubeResponseRows", new ArrayList<>());
    template.process(model, out);
  }
}
