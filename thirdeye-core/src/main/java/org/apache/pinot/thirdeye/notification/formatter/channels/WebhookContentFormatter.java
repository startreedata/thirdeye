package org.apache.pinot.thirdeye.notification.formatter.channels;

import com.google.inject.Singleton;
import java.util.List;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.WebhookApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

@Singleton
public class WebhookContentFormatter {

  public WebhookApi getWebhookApi(final List<MergedAnomalyResultDTO> anomalies, SubscriptionGroupDTO subsConfig){
    return ApiBeanMapper.toWebhookApi(anomalies, subsConfig);
  }
}
