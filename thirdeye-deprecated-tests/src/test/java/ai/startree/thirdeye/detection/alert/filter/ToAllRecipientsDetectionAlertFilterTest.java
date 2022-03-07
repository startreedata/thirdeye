/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.filter;

import static ai.startree.thirdeye.detection.alert.filter.AlertFilterUtils.makeAnomaly;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilter;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterNotification;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ToAllRecipientsDetectionAlertFilterTest {

  private static final String PROP_RECIPIENTS = "recipients";
  private static final String PROP_EMAIL_SCHEME = "emailScheme";
  private static final String PROP_JIRA_SCHEME = "jiraScheme";
  private static final String PROP_ASSIGNEE = "assignee";
  private static final String PROP_TO = "to";
  private static final String PROP_CC = "cc";
  private static final String PROP_BCC = "bcc";
  private static final List<String> PROP_EMPTY_TO_VALUE = new ArrayList<>();
  private static final List<String> PROP_TO_VALUE =
      Arrays.asList("test@test.com", "test@test.org");
  private static final List<String> PROP_CC_VALUE =
      Arrays.asList("cctest@test.com", "cctest@test.org");
  private static final List<String> PROP_BCC_VALUE =
      Arrays.asList("bcctest@test.com", "bcctest@test.org");
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private TestDbEnv testDAOProvider = null;

  private DetectionAlertFilter alertFilter;
  private List<MergedAnomalyResultDTO> detection1Anomalies;
  private List<MergedAnomalyResultDTO> detection2Anomalies;
  private List<MergedAnomalyResultDTO> detection3Anomalies;
  private MockDataProvider provider;
  private SubscriptionGroupDTO alertConfig;
  private long detectionConfigId1;
  private long detectionConfigId2;
  private long detectionConfigId3;
  private long detectionConfigId;
  private long baseTime;
  private static List<Long> PROP_ID_VALUE;

  @BeforeMethod
  public void beforeMethod() throws InterruptedException {
    testDAOProvider = new TestDbEnv();

    AlertDTO detectionConfig1 = new AlertDTO();
    detectionConfig1.setName("test detection 1");
    detectionConfig1.setActive(true);
    this.detectionConfigId1 = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig1);

    AlertDTO detectionConfig2 = new AlertDTO();
    detectionConfig2.setName("test detection 2");
    detectionConfig2.setActive(true);
    this.detectionConfigId2 = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig2);

    AlertDTO detectionConfig3 = new AlertDTO();
    detectionConfig3.setName("test detection 3");
    detectionConfig3.setActive(true);
    this.detectionConfigId3 = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig3);

    AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName("test detection 0");
    detectionConfig.setActive(true);
    this.detectionConfigId = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig);

    PROP_ID_VALUE = Arrays.asList(this.detectionConfigId1, this.detectionConfigId2);

    // Anomaly notification is tracked through create time. Start and end time doesn't matter here.
    this.detection1Anomalies = new ArrayList<>();
    this.detection2Anomalies = new ArrayList<>();
    this.detection3Anomalies = new ArrayList<>();
    this.baseTime = System.currentTimeMillis();
    Thread.sleep(100);
    this.detection1Anomalies.add(makeAnomaly(detectionConfigId1, this.baseTime, 0, 100));
    this.detection2Anomalies.add(makeAnomaly(detectionConfigId2, this.baseTime, 0, 100));
    Thread.sleep(50);
    this.detection2Anomalies.add(makeAnomaly(detectionConfigId2, this.baseTime, 110, 150));
    Thread.sleep(50);
    this.detection1Anomalies.add(makeAnomaly(detectionConfigId1, this.baseTime, 150, 200));
    Thread.sleep(250);
    this.detection2Anomalies.add(makeAnomaly(detectionConfigId2, this.baseTime, 300, 450));
    this.detection3Anomalies.add(makeAnomaly(detectionConfigId3, this.baseTime, 300, 450));

    Thread.sleep(100);
    this.alertConfig = createDetectionAlertConfig();
  }

  private SubscriptionGroupDTO createDetectionAlertConfig() {
    SubscriptionGroupDTO alertConfig = new SubscriptionGroupDTO();

    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_DETECTION_CONFIG_IDS, PROP_ID_VALUE);
    alertConfig.setProperties(properties);

    alertConfig.setNotificationSchemes(new NotificationSchemesDto()
        .setEmailScheme(new EmailSchemeDto()
            .setTo(PROP_TO_VALUE)
            .setCc(PROP_CC_VALUE)
            .setBcc(PROP_BCC_VALUE)));

    Map<Long, Long> vectorClocks = new HashMap<>();
    vectorClocks.put(detectionConfigId1, this.baseTime);
    vectorClocks.put(detectionConfigId2, this.baseTime);
    alertConfig.setVectorClocks(vectorClocks);

    return alertConfig;
  }

//  TODO refactor test case when jira notification is supported
//  private SubscriptionGroupDTO createDetectionAlertConfigWithJira() {
//    SubscriptionGroupDTO alertConfig = new SubscriptionGroupDTO();
//
//    Map<String, Object> properties = new HashMap<>();
//    properties.put(PROP_DETECTION_CONFIG_IDS, PROP_ID_VALUE);
//    alertConfig.setProperties(properties);
//
//    Map<String, Object> alertSchemes = new HashMap<>();
//    Map<String, Object> jiraScheme = new HashMap<>();
//    jiraScheme.put(PROP_ASSIGNEE, "test");
//    alertSchemes.put(PROP_JIRA_SCHEME, jiraScheme);
//    alertConfig.setNotificationSchemes(alertSchemes);
//
//    Map<Long, Long> vectorClocks = new HashMap<>();
//    vectorClocks.put(detectionConfigId1, this.baseTime);
//    vectorClocks.put(detectionConfigId2, this.baseTime);
//    alertConfig.setVectorClocks(vectorClocks);
//
//    return alertConfig;
//  }

  /**
   * Test if all the created anomalies are picked up the the email notification filter
   */
  @Test
  public void testGetAlertFilterResult() throws Exception {
    this.alertFilter = new ToAllRecipientsDetectionAlertFilter(this.provider, this.alertConfig,
        this.baseTime + 350L, TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO(), TestDbEnv.getInstance().getDetectionConfigManager());
    DetectionAlertFilterResult result = this.alertFilter.run();
    DetectionAlertFilterNotification notification = AlertFilterUtils.makeEmailNotifications(
        this.alertConfig, PROP_TO_VALUE, PROP_CC_VALUE, PROP_BCC_VALUE);

    Assert.assertEquals(result.getResult().get(notification).size(), 4);
    Set<MergedAnomalyResultDTO> expectedAnomalies = new HashSet<>();
    expectedAnomalies.addAll(this.detection1Anomalies.subList(0, 2));
    expectedAnomalies.addAll(this.detection2Anomalies.subList(0, 2));
    Assert.assertEquals(result.getResult().get(notification), expectedAnomalies);
  }

  /**
   * Test if all the created anomalies are picked up the the jira notification filter
   */
//  TODO refactor test case when jira notification is supported
//  @Test
//  public void testGetAlertFilterResultWithJira() throws Exception {
//    SubscriptionGroupDTO alertConfig = createDetectionAlertConfigWithJira();
//    this.alertFilter = new ToAllRecipientsDetectionAlertFilter(this.provider, alertConfig,
//        this.baseTime + 350L, TestDbEnv.getInstance()
//            .getMergedAnomalyResultDAO(), TestDbEnv.getInstance().getDetectionConfigManager());
//
//    DetectionAlertFilterResult result = this.alertFilter.run();
//    DetectionAlertFilterNotification notification = AlertFilterUtils
//        .makeJiraNotifications(this.alertConfig, "test");
//
//    Assert.assertEquals(result.getResult().get(notification).size(), 4);
//    Set<MergedAnomalyResultDTO> expectedAnomalies = new HashSet<>();
//    expectedAnomalies.addAll(this.detection1Anomalies.subList(0, 2));
//    expectedAnomalies.addAll(this.detection2Anomalies.subList(0, 2));
//    Assert.assertEquals(result.getResult().get(notification), expectedAnomalies);
//  }

  /**
   * Tests if the watermarks are working correctly (anomalies are not re-notified)
   */
  @Test
  public void testAlertFilterNoResend() throws Exception {
    // Assume below 2 anomalies have already been notified
    makeAnomaly(detectionConfigId1, this.baseTime, 10, 11);
    makeAnomaly(detectionConfigId1, this.baseTime, 11, 12);
    this.alertConfig.getProperties()
        .put(PROP_DETECTION_CONFIG_IDS, Collections.singletonList(detectionConfigId1));
    this.alertConfig
        .setVectorClocks(Collections.singletonMap(detectionConfigId1, System.currentTimeMillis()));
    Thread.sleep(1);  // Make sure the next anomaly is not created at the same time as watermark

    // This newly detected anomaly needs to be notified to the user
    MergedAnomalyResultDTO existingFuture = makeAnomaly(detectionConfigId1, this.baseTime, 12, 13);
    Thread.sleep(1);  // Make sure the next anomaly is not created at the same time as watermark

    this.alertFilter = new ToAllRecipientsDetectionAlertFilter(this.provider, this.alertConfig,
        System.currentTimeMillis(), TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO(), TestDbEnv.getInstance().getDetectionConfigManager());

    DetectionAlertFilterResult result = this.alertFilter.run();
    DetectionAlertFilterNotification notification = AlertFilterUtils.makeEmailNotifications(
        this.alertConfig, PROP_TO_VALUE, PROP_CC_VALUE, PROP_BCC_VALUE);

    Assert.assertTrue(result.getResult().containsKey(notification));
    Assert.assertEquals(result.getResult().get(notification).size(), 1);
    Assert.assertTrue(result.getResult().get(notification).contains(existingFuture));
  }

  /**
   * Test if the filter generates entries irrespective of the recipients & anomalies being present
   * or not
   */
  @Test
  public void testGetAlertFilterResultWhenNoRecipient() throws Exception {
    Map<String, Object> properties = ConfigUtils
        .getMap(this.alertConfig.getProperties().get(PROP_RECIPIENTS));
    properties.put(PROP_TO, PROP_EMPTY_TO_VALUE);
    this.alertConfig.setNotificationSchemes(new NotificationSchemesDto()
        .setEmailScheme(new EmailSchemeDto()
            .setTo(new ArrayList<>())
            .setCc(PROP_CC_VALUE)
            .setBcc(PROP_BCC_VALUE)));
    this.alertConfig.setProperties(properties);

    this.alertFilter = new ToAllRecipientsDetectionAlertFilter(this.provider, this.alertConfig,
        this.baseTime + 25L, TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO(), TestDbEnv.getInstance().getDetectionConfigManager());
    DetectionAlertFilterResult result = this.alertFilter.run();

    DetectionAlertFilterNotification notification = AlertFilterUtils.makeEmailNotifications(
        this.alertConfig, new ArrayList<>(), PROP_CC_VALUE, PROP_BCC_VALUE);
    Assert.assertEquals(result.getResult().size(), 1);
    Assert.assertEquals(result.getResult().get(notification), Collections.emptySet());
  }

  /**
   * Test to ensure this filter doesn't pick up anomalies with feedback (we do not want to notify
   * them)
   */
  @Test(enabled = false)
  public void testAlertFilterFeedback() throws Exception {
    this.alertConfig.getProperties()
        .put(PROP_DETECTION_CONFIG_IDS, Collections.singletonList(detectionConfigId3));
    this.alertConfig.setVectorClocks(Collections.singletonMap(detectionConfigId3, this.baseTime));

    // Create feedback objects
    AnomalyFeedbackDTO feedbackAnomaly = new AnomalyFeedbackDTO()
        .setFeedbackType(AnomalyFeedbackType.ANOMALY);
    AnomalyFeedbackDTO feedbackNoFeedback = new AnomalyFeedbackDTO()
        .setFeedbackType(AnomalyFeedbackType.NO_FEEDBACK);

    // Create anomalies with various feedback type
    MergedAnomalyResultDTO anomalyWithFeedback = makeAnomaly(detectionConfigId3, this.baseTime, 5,
        10, Collections.emptyMap(), feedbackAnomaly);
    MergedAnomalyResultDTO anomalyWithNoFeedback = makeAnomaly(detectionConfigId3, this.baseTime, 5,
        10, Collections.emptyMap(), feedbackNoFeedback);
    MergedAnomalyResultDTO anomalyWithNullFeedback = makeAnomaly(detectionConfigId3, this.baseTime,
        5, 10, Collections.emptyMap(), null);
    Thread.sleep(1);

    this.detection3Anomalies.add(anomalyWithFeedback);
    this.detection3Anomalies.add(anomalyWithNoFeedback);
    this.detection3Anomalies.add(anomalyWithNullFeedback);

    this.alertFilter = new ToAllRecipientsDetectionAlertFilter(this.provider, this.alertConfig,
        System.currentTimeMillis(), TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO(), TestDbEnv.getInstance().getDetectionConfigManager());
    DetectionAlertFilterResult result = this.alertFilter.run();
    Assert.assertEquals(result.getResult().size(), 1);

    DetectionAlertFilterNotification notification = AlertFilterUtils
        .makeEmailNotifications(this.alertConfig, PROP_TO_VALUE, PROP_CC_VALUE, PROP_BCC_VALUE);
    Assert.assertTrue(result.getResult().containsKey(notification));
    Assert.assertEquals(result.getResult().get(notification).size(), 3);
    // Filter should pick up all anomalies which do not have labels
    Assert
        .assertTrue(result.getResult().get(notification).contains(this.detection3Anomalies.get(0)));
    Assert.assertTrue(result.getResult().get(notification).contains(anomalyWithNoFeedback));
    Assert.assertTrue(result.getResult().get(notification).contains(anomalyWithNullFeedback));
    // Anomalies which have been labeled should not be picked up by the filter
    Assert.assertFalse(result.getResult().get(notification).contains(anomalyWithFeedback));
  }
}
