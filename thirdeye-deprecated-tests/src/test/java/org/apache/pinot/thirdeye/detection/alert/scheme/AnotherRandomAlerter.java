package org.apache.pinot.thirdeye.detection.alert.scheme;

import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;

/** Used in tests
 *
 */
@SuppressWarnings("unused")
public class AnotherRandomAlerter extends DetectionAlertScheme {

  public AnotherRandomAlerter(SubscriptionGroupDTO subsConfig,
      ThirdEyeWorkerConfiguration thirdeyeConfig,
      DetectionAlertFilterResult result) {
    super(subsConfig, result, DAORegistry.getInstance().getMetricConfigDAO(),
        DAORegistry.getInstance().getDetectionConfigManager(),
        DAORegistry.getInstance().getEventDAO(),
        DAORegistry.getInstance().getMergedAnomalyResultDAO()
    );
  }

  @Override
  public void run() {

  }
}
