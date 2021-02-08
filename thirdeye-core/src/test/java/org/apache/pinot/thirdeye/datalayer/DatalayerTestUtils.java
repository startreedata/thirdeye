package org.apache.pinot.thirdeye.datalayer;

import org.apache.pinot.thirdeye.common.metric.MetricType;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;

public class DatalayerTestUtils {

  public static MetricConfigDTO getTestMetricConfig(String collection, String metric, Long id) {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    if (id != null) {
      metricConfigDTO.setId(id);
    }
    metricConfigDTO.setDataset(collection);
    metricConfigDTO.setDatatype(MetricType.LONG);
    metricConfigDTO.setName(metric);
    metricConfigDTO.setAlias(ThirdEyeUtils.constructMetricAlias(collection, metric));
    return metricConfigDTO;
  }
}
