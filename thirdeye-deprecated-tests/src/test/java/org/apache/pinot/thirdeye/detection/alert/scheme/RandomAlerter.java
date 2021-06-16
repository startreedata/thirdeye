package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

public class RandomAlerter extends DetectionAlertScheme {

  public RandomAlerter(SubscriptionGroupDTO subsConfig, ThirdEyeCoordinatorConfiguration thirdeyeConfig,
                       DetectionAlertFilterResult result,
                       final MetricConfigManager metricConfigManager, final AlertManager detectionConfigManager,
                       final EventManager eventManager,
                       final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(subsConfig, result, TestDbEnv.getInstance().getMetricConfigDAO(),
        TestDbEnv.getInstance().getDetectionConfigManager(),
        TestDbEnv.getInstance().getEventDAO(),
        TestDbEnv.getInstance().getMergedAnomalyResultDAO());
  }

  @Override
  public void run() {

  }
}
