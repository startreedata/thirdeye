package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

public class RandomAlerter extends DetectionAlertScheme {

  public RandomAlerter(
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
