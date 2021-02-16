package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;

public class RandomAlerter extends DetectionAlertScheme {

  public RandomAlerter(SubscriptionGroupDTO subsConfig, ThirdEyeWorkerConfiguration thirdeyeConfig,
      DetectionAlertFilterResult result) {
    super(subsConfig, result, TestDbEnv.getInstance().getMetricConfigDAO(),
        TestDbEnv.getInstance().getDetectionConfigManager(),
        TestDbEnv.getInstance().getEventDAO(),
        TestDbEnv.getInstance().getMergedAnomalyResultDAO());
  }

  @Override
  public void run() {

  }
}
