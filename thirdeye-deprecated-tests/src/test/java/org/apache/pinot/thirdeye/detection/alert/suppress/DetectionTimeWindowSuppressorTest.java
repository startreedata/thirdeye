package org.apache.pinot.thirdeye.detection.alert.suppress;

import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.ACCEPTABLE_DEVIATION_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.EXPECTED_CHANGE_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.IS_THRESHOLD_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.TIME_WINDOWS_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.TIME_WINDOW_SUPPRESSOR_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.WINDOW_END_TIME_KEY;
import static org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertTimeWindowSuppressor.WINDOW_START_TIME_KEY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectionTimeWindowSuppressorTest {

  private TestDbEnv testDAOProvider;
  private Set<MergedAnomalyResultDTO> anomalies;
  private SubscriptionGroupDTO config;

  private Map<String, Object> createSuppressWindow(long startTime, long endTime,
      boolean isThreshold, double expectedChange,
      double acceptableDeviation) {
    Map<String, Object> suppressWindowProps = new HashMap<>();
    suppressWindowProps.put(WINDOW_START_TIME_KEY, startTime);
    suppressWindowProps.put(WINDOW_END_TIME_KEY, endTime);
    suppressWindowProps.put(IS_THRESHOLD_KEY, isThreshold);
    suppressWindowProps.put(EXPECTED_CHANGE_KEY, expectedChange);
    suppressWindowProps.put(ACCEPTABLE_DEVIATION_KEY, acceptableDeviation);
    return suppressWindowProps;
  }

  private MergedAnomalyResultDTO createAnomaly(long id, long startTime, long endTime,
      double weight) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setId(id);
    anomaly.setStartTime(startTime);
    anomaly.setEndTime(endTime);
    anomaly.setWeight(weight);
    return anomaly;
  }

  private void initDetectionAlertConfig() {
    config = new SubscriptionGroupDTO();

    List<Map<String, Object>> suppressWindowList = new ArrayList<>();
    suppressWindowList.add(createSuppressWindow(1000, 3000, true, 0.5, 0.1));
    suppressWindowList.add(createSuppressWindow(4500, 6000, true, 0.6, 0.2));

    Map<String, Object> params = new HashMap<>();
    params.put(TIME_WINDOWS_KEY, suppressWindowList);

    Map<String, Object> alertSuppressors = new HashMap<>();
    alertSuppressors.put(TIME_WINDOW_SUPPRESSOR_KEY, params);
    config.setAlertSuppressors(alertSuppressors);
  }

  private void initAnomalies() {
    anomalies = new HashSet<>();

    anomalies.add(createAnomaly(1l, 500, 900, 0.5));
    anomalies.add(createAnomaly(2l, 700, 1000, 0.8));
    anomalies.add(createAnomaly(3l, 500, 1500, 0.2));
    anomalies.add(createAnomaly(4l, 1000, 1500, 0.4));
    anomalies.add(createAnomaly(5l, 1500, 2500, 0.6));
    anomalies.add(createAnomaly(6l, 2500, 3000, 0.7));
    anomalies.add(createAnomaly(7l, 2000, 3500, 0.5));
    anomalies.add(createAnomaly(8l, 3000, 3500, 0.6));
    anomalies.add(createAnomaly(9l, 3500, 4000, 0.1));
    anomalies.add(createAnomaly(10l, 5000, 5500, 0.5));
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    this.testDAOProvider = new TestDbEnv();
  }

  @BeforeMethod
  public void beforeMethod() throws Exception {
    initAnomalies();
    initDetectionAlertConfig();
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    testDAOProvider.cleanup();
  }

  /**
   * Anomaly distribution along with suppression windows.
   *
   * Anomalies 4, 5, 7, and 10 should be suppressed (not notified).
   * Anomaly 6 is not suppressed because it falls outside the suppression region.
   *
   * *-----3----*    *------7-------*
   * |
   * | *-2-*    *----5----*    *--8-*
   * |     |                   |
   * *-1-* *--4-*         *--6-*    *--9-*        *---10---*
   * |     |                   |
   * _____|_____|___________________|______________|________________|
   * |     |                   |              |                |
   * 500     |                   |              |                |
   * 1000----<window1>----3000          4500--<window2>--6000
   */
  @Test
  public void testTimeWindowSuppressorWithThreshold() throws Exception {
    NotificationSchemesDto alertProps = new NotificationSchemesDto()
        .setEmailScheme(new EmailSchemeDto()
                .setTo(Collections.singletonList("test@test.test")));
    SubscriptionGroupDTO subsConfig = new SubscriptionGroupDTO();
    subsConfig.setNotificationSchemes(alertProps);
    DetectionAlertFilterResult result = new DetectionAlertFilterResult();
    result.addMapping(new DetectionAlertFilterNotification(subsConfig), anomalies);

    DetectionAlertTimeWindowSuppressor suppressor = new DetectionAlertTimeWindowSuppressor(config,
        TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO());
    DetectionAlertFilterResult resultsAfterSuppress = suppressor.run(result);

    Set<Long> filteredAnomalyIds = new HashSet<>(Arrays.asList(1l, 2l, 3l, 6l, 8l, 9l));

    Assert.assertEquals(resultsAfterSuppress.getAllAnomalies().size(), 6);
    for (MergedAnomalyResultDTO anomaly : resultsAfterSuppress.getAllAnomalies()) {
      Assert.assertTrue(filteredAnomalyIds.contains(anomaly.getId()));
    }
  }

  /**
   * Overlapping time window suppressor without thresholds
   */
  @Test
  public void testTimeWindowSuppressor() throws Exception {
    List<Map<String, Object>> suppressWindowList = new ArrayList<>();
    suppressWindowList.add(createSuppressWindow(500, 6000, false, 0, 0));

    Map<String, Object> params = new HashMap<>();
    params.put(TIME_WINDOWS_KEY, suppressWindowList);

    Map<String, Object> alertSuppressors = new HashMap<>();
    alertSuppressors.put(TIME_WINDOW_SUPPRESSOR_KEY, params);
    config.setAlertSuppressors(alertSuppressors);


    DetectionAlertFilterResult result = new DetectionAlertFilterResult();
    SubscriptionGroupDTO subsConfig = new SubscriptionGroupDTO();
    NotificationSchemesDto alertProps = new NotificationSchemesDto()
        .setEmailScheme(new EmailSchemeDto()
            .setTo(Collections.singletonList("test@test.test")));
    subsConfig.setNotificationSchemes(alertProps);
    subsConfig.setNotificationSchemes(alertProps);
    result.addMapping(new DetectionAlertFilterNotification(subsConfig), anomalies);

    DetectionAlertTimeWindowSuppressor suppressor = new DetectionAlertTimeWindowSuppressor(config,
        TestDbEnv.getInstance()
            .getMergedAnomalyResultDAO());
    DetectionAlertFilterResult resultsAfterSuppress = suppressor.run(result);

    Assert.assertEquals(resultsAfterSuppress.getAllAnomalies().size(), 0);
  }
}
