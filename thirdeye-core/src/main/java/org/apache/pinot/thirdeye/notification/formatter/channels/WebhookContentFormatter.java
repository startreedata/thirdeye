package org.apache.pinot.thirdeye.notification.formatter.channels;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.notification.commons.WebhookEntity;
import org.apache.pinot.thirdeye.notification.content.AnomalyReportEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;

public class WebhookContentFormatter {

  private final ThirdEyeCoordinatorConfiguration teConfig;
  @Inject
  public WebhookContentFormatter(
      final ThirdEyeCoordinatorConfiguration teConfig) {
    this.teConfig = teConfig;
  }

  public WebhookEntity getWebhookEntity(final Collection<AnomalyResult> anomalies, final BaseNotificationContent content, SubscriptionGroupDTO subsConfig, Properties properties){
    content.init(properties, teConfig);
    final Map<String, Object> templateData = content.format(anomalies, subsConfig);
    return new WebhookEntity()
        .setSubscriptionGroup(subsConfig.getName())
        .setResult((List<AnomalyReportEntity>) templateData.get("anomalyDetails"));
  }
}
