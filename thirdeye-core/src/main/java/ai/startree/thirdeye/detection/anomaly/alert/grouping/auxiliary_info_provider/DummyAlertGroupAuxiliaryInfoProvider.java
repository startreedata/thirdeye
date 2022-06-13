/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping.auxiliary_info_provider;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.List;

public class DummyAlertGroupAuxiliaryInfoProvider extends BaseAlertGroupAuxiliaryInfoProvider {

  @Override
  public AuxiliaryAlertGroupInfo getAlertGroupAuxiliaryInfo(DimensionMap dimensions,
      List<MergedAnomalyResultDTO> anomalyResultList) {
    return EMPTY_AUXILIARY_ALERT_GROUP_INFO;
  }
}
