package org.apache.pinot.thirdeye.notification.formatter.channels;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.notification.commons.WebhookEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent.AnomalyReportEntity;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;

public class WebhookContentFormatter extends AlertContentFormatter {

  public WebhookContentFormatter(final Properties alertClientConfig,
      final BaseNotificationContent content,
      final ThirdEyeCoordinatorConfiguration teConfig,
      final SubscriptionGroupDTO subsConfig) {
    super(alertClientConfig, content, teConfig, subsConfig);
  }

  public WebhookEntity getWebhookEntity(final Collection<AnomalyResult> anomalies){
    final Map<String, Object> templateData = notificationContent.format(anomalies, subsConfig);
    return new WebhookEntity()
        .setSubscriptionGroup(subsConfig.getName())
        .setResult((List<AnomalyReportEntity>) templateData.get("anomalyDetails"));
  }
}
