package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;

public class RandomAlerter extends DetectionAlertScheme {

  public RandomAlerter(SubscriptionGroupDTO subsConfig, ThirdEyeWorkerConfiguration thirdeyeConfig,
      DetectionAlertFilterResult result) {
    super(subsConfig, result);
  }

  @Override
  public void run() {

  }
}
