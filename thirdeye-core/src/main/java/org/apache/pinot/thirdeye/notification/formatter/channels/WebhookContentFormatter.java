package org.apache.pinot.thirdeye.notification.formatter.channels;

import com.google.inject.Inject;
import java.util.List;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.WebhookApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

public class WebhookContentFormatter {

  private final ThirdEyeCoordinatorConfiguration teConfig;
  @Inject
  public WebhookContentFormatter(
      final ThirdEyeCoordinatorConfiguration teConfig) {
    this.teConfig = teConfig;
  }

  public WebhookApi getWebhookApi(final List<MergedAnomalyResultDTO> anomalies, SubscriptionGroupDTO subsConfig){
    return ApiBeanMapper.toWebhookApi(anomalies, subsConfig);
  }
}
