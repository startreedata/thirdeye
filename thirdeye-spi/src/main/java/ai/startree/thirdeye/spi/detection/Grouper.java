/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.List;

public interface Grouper<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * group anomalies.
   *
   * @return list of anomalies, with grouped dimensions
   */
  List<MergedAnomalyResultDTO> group(List<MergedAnomalyResultDTO> anomalies);
}
