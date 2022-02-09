package ai.startree.thirdeye.detection.alert.scheme;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

/**
 * Used in tests
 */
@SuppressWarnings("unused")
public class AnotherRandomAlerter extends NotificationScheme {

  @Override
  public void run(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) {

  }
}
