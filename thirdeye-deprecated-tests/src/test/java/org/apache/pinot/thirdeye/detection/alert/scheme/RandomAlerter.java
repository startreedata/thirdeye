package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

public class RandomAlerter extends NotificationScheme {

  @Override
  public void run(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) {

  }
}
