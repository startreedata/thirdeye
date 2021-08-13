package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

/**
 * Used in tests
 */
@SuppressWarnings("unused")
public class AnotherRandomAlerter extends DetectionAlertScheme {

  public AnotherRandomAlerter(
      final MetricAnomaliesContent metricAnomaliesContent,
      final EntityGroupKeyContent entityGroupKeyContent) {
    super(metricAnomaliesContent, entityGroupKeyContent);
  }

  @Override
  public void run(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) {

  }
}
