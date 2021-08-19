package org.apache.pinot.thirdeye.notification.formatter.channels;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AnomalyWrapperApi;
import org.apache.pinot.thirdeye.spi.api.WebhookApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

@Singleton
public class WebhookContentFormatter {

  public static String ANOMALY_DASHBOARD_PREFIX = "anomalies/view/id/";
  private final ThirdEyeCoordinatorConfiguration teConfig;

  @Inject
  public WebhookContentFormatter(
      final ThirdEyeCoordinatorConfiguration teConfig
  ) {
    this.teConfig = teConfig;
  }

  public WebhookApi getWebhookApi(final List<MergedAnomalyResultDTO> anomalies,
      SubscriptionGroupDTO subsConfig) {
    WebhookApi api = ApiBeanMapper.toWebhookApi(anomalies, subsConfig);
    List<AnomalyWrapperApi> results = api.getResult();
    return api.setResult(results.stream()
        .map(result -> result.setUrl(getDashboardUrl(result.getAnomaly().getId())))
        .collect(
            Collectors.toList()));
  }

  private String getDashboardUrl(final Long id) {
    String extUrl =  teConfig.getUiConfiguration().getExternalUrl();
    if(!extUrl.matches(".*/")){
      extUrl += "/";
    }
    return String.format("%s%s%s",extUrl, ANOMALY_DASHBOARD_PREFIX, id);
  }
}
