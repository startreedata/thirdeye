/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert.filter;

import static ai.startree.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_BCC_VALUE;
import static ai.startree.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_CC_VALUE;
import static ai.startree.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_TO_VALUE;
import static ai.startree.thirdeye.detection.alert.filter.AlertFilterUtils.makeAnomaly;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilter;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterNotification;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalySeverity;
import com.google.common.collect.ImmutableMap;
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

public class AnomalySeverityAlertFilterTest {

  private static final Set<String> PROP_TO_FOR_VALUE =
      new HashSet<>(Arrays.asList("myTest@example.com", "myTest@example.org"));
  private static final Set<String> PROP_TO_FOR_ANOTHER_VALUE =
      new HashSet<>(Arrays.asList("myTest@example.net", "myTest@example.com"));
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private static final String PROP_SEVERITY_TO = "severityRecipients";
  private static final List<Object> severityProperty = new ArrayList<>();

  private DetectionAlertFilter alertFilter;

  private TestDbEnv testDAOProvider;
  private SubscriptionGroupDTO alertConfig;
  private List<MergedAnomalyResultDTO> detectionAnomalies;
  private long baseTime;
  private List<Long> detectionConfigIds;
  private MergedAnomalyResultDTO renotifyAnomaly;
  private final MockDataProvider provider = new MockDataProvider();
  private final Map<String, Object> notify1 = new HashMap<>();
  private final Map<String, Object> notify2 = new HashMap<>();
  private final NotificationSchemesDto defaultScheme = new NotificationSchemesDto().setEmailScheme(
      new EmailSchemeDto().setTo(Arrays.asList("test@example.com", "test@example.org"))
          .setCc(Arrays.asList("cctest@example.com", "cctest@example.org"))
          .setBcc(Arrays.asList("bcctest@example.com", "bcctest@example.org")));

  @BeforeMethod
  public void beforeMethod() throws InterruptedException {
    testDAOProvider = new TestDbEnv();

    AlertDTO detectionConfig1 = new AlertDTO();
    detectionConfig1.setName("test detection 1");
    detectionConfig1.setActive(true);
    long detectionConfigId1 = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig1);

    AlertDTO detectionConfig2 = new AlertDTO();
    detectionConfig2.setName("test detection 2");
    detectionConfig2.setActive(true);
    long detectionConfigId2 = TestDbEnv.getInstance().getDetectionConfigManager()
        .save(detectionConfig2);

    detectionConfigIds = Arrays.asList(detectionConfigId1, detectionConfigId2);

    // Anomaly notification is tracked through create time. Start and end time doesn't matter here.
    this.detectionAnomalies = new ArrayList<>();
    renotifyAnomaly =
        makeAnomaly(detectionConfigId1, System.currentTimeMillis(), 0, 50,
            Collections.singletonMap("key", "value"),
            null, AnomalySeverity.LOW);
    Thread.sleep(100);
    this.baseTime = System.currentTimeMillis();
    Thread.sleep(100);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId1, this.baseTime, 0, 100,
            Collections.singletonMap("key", "value"), null,
            AnomalySeverity.LOW));
    Thread.sleep(10);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId1, this.baseTime, 0, 110,
            Collections.singletonMap("key", "anotherValue"), null,
            AnomalySeverity.MEDIUM));
    Thread.sleep(20);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId1, this.baseTime, 0, 120,
            Collections.singletonMap("key", "unknownValue"), null,
            AnomalySeverity.HIGH));
    Thread.sleep(30);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId2, this.baseTime, 110, 150,
            Collections.singletonMap("unknownKey", "value"),
            null));
    Thread.sleep(10);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId2, this.baseTime, 120, 160,
            Collections.singletonMap("key", "value"), null));
    Thread.sleep(40);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId1, this.baseTime, 150, 200,
            Collections.emptyMap(), null));
    Thread.sleep(200);
    this.detectionAnomalies.add(
        makeAnomaly(detectionConfigId2, this.baseTime, 300, 400,
            Collections.singletonMap("key", "value"), null));
    Thread.sleep(100);

    this.alertConfig = createDetectionAlertConfig();
  }

  private SubscriptionGroupDTO createDetectionAlertConfig() {
    SubscriptionGroupDTO alertConfig = new SubscriptionGroupDTO();

    notify1.put("severity", Arrays.asList("LOW", "MEDIUM"));
    notify1.put("notify",
        ImmutableMap.of("emailScheme", ImmutableMap.of("recipients", PROP_TO_FOR_VALUE)));
    notify2.put("severity", Collections.singleton("HIGH"));
    notify2.put("notify",
        ImmutableMap.of("emailScheme", ImmutableMap.of("recipients", PROP_TO_FOR_ANOTHER_VALUE)));
    severityProperty.add(notify1);
    severityProperty.add(notify2);

    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_DETECTION_CONFIG_IDS, detectionConfigIds);
    properties.put(PROP_SEVERITY_TO, severityProperty);
    alertConfig.setProperties(properties);

    alertConfig.setNotificationSchemes(new NotificationSchemesDto()
        .setEmailScheme(new EmailSchemeDto()
            .setTo(PROP_TO_VALUE)
            .setCc(PROP_CC_VALUE)
            .setBcc(PROP_BCC_VALUE)));

    Map<Long, Long> vectorClocks = new HashMap<>();
    vectorClocks.put(detectionConfigIds.get(0), this.baseTime);
    vectorClocks.put(detectionConfigIds.get(1), this.baseTime);
    alertConfig.setVectorClocks(vectorClocks);

    return alertConfig;
  }

  @Test
  public void testAlertFilterRecipients() throws Exception {
    this.alertFilter = new AnomalySeverityAlertFilter(provider, alertConfig, this.baseTime + 350L,
        TestDbEnv.getInstance().getAnomalySubscriptionGroupNotificationManager(),
        TestDbEnv.getInstance().getMergedAnomalyResultDAO(),
        TestDbEnv.getInstance().getDetectionConfigManager());

    DetectionAlertFilterResult result = this.alertFilter.run();
    Assert.assertEquals(result.getResult().size(), 2);

    int verifiedResult = 0;
    for (Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> entry : result
        .getResult()
        .entrySet()) {
      if (entry.getValue().equals(makeSet(0, 1))) {
        Assert.assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(),
            notify1.get("notify"));
        verifiedResult++;
      } else if (entry.getValue().equals(makeSet(2))) {
        Assert.assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(),
            notify2.get("notify"));
        verifiedResult++;
      } else if (entry.getValue().equals(makeSet(3, 4, 5))) {
        Assert
            .assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(), defaultScheme);
        verifiedResult++;
      }
    }
    Assert.assertEquals(verifiedResult, 1);
  }

  @Test
  public void testRenotifyAnomaly() throws Exception {
    AnomalySubscriptionGroupNotificationManager renotificationManager =
        TestDbEnv.getInstance().getAnomalySubscriptionGroupNotificationManager();
    AnomalySubscriptionGroupNotificationDTO anomalySubscriptionGroupNotification =
        new AnomalySubscriptionGroupNotificationDTO();
    anomalySubscriptionGroupNotification.setAnomalyId(renotifyAnomaly.getId());
    anomalySubscriptionGroupNotification
        .setDetectionConfigId(renotifyAnomaly.getDetectionConfigId());
    renotificationManager.save(anomalySubscriptionGroupNotification);

    this.alertFilter = new AnomalySeverityAlertFilter(provider, alertConfig, this.baseTime + 350L,
        TestDbEnv.getInstance().getAnomalySubscriptionGroupNotificationManager(),
        TestDbEnv.getInstance().getMergedAnomalyResultDAO(),
        TestDbEnv.getInstance().getDetectionConfigManager());

    DetectionAlertFilterResult result = this.alertFilter.run();
    Assert.assertEquals(result.getResult().size(), 2);

    int verifiedResult = 0;
    for (Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> entry : result
        .getResult()
        .entrySet()) {
      if (entry.getValue().equals(makeSet(renotifyAnomaly, 0, 1))) {
        Assert.assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(),
            notify1.get("notify"));
        verifiedResult++;
      } else if (entry.getValue().equals(makeSet(2))) {
        Assert.assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(),
            notify2.get("notify"));
        verifiedResult++;
      } else if (entry.getValue().equals(makeSet(3, 4, 5))) {
        Assert
            .assertEquals(entry.getKey().getSubscriptionConfig().getNotificationSchemes(), defaultScheme);
        verifiedResult++;
      }
    }
    Assert.assertEquals(verifiedResult, 1);
  }

  private Set<MergedAnomalyResultDTO> makeSet(MergedAnomalyResultDTO anomaly,
      int... anomalyIndices) {
    Set<MergedAnomalyResultDTO> set = makeSet(anomalyIndices);
    set.add(anomaly);
    return set;
  }

  private Set<MergedAnomalyResultDTO> makeSet(int... anomalyIndices) {
    Set<MergedAnomalyResultDTO> output = new HashSet<>();
    for (int anomalyIndex : anomalyIndices) {
      output.add(this.detectionAnomalies.get(anomalyIndex));
    }
    return output;
  }
}
