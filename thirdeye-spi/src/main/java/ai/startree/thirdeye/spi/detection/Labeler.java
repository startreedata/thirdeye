/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;
import java.util.Map;

public interface Labeler<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Calculate the severity for list of anomalies
   *
   * @param anomalies input anoamlies
   * @return mapping from anomaly to severity
   */
  Map<MergedAnomalyResultDTO, AnomalySeverity> label(List<MergedAnomalyResultDTO> anomalies);
}
