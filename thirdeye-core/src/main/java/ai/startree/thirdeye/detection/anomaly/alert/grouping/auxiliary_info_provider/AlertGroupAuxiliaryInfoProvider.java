/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping.auxiliary_info_provider;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.List;
import java.util.Map;

/**
 * Given a dimension map, a provider returns a list of email recipients (separated by commas) for
 * that dimension.
 */
public interface AlertGroupAuxiliaryInfoProvider {

  /**
   * Sets the properties of this grouper.
   *
   * @param props the properties for this grouper.
   */
  void setParameters(Map<String, String> props);

  /**
   * Returns a list of email recipients (separated by commas) for the given dimension.
   *
   * @param dimensions the dimension of the group, which is used to look for the recipients.
   * @param anomalyResultList the list of anomalies of this group, whose information could be
   *     used to determine the
   *     recipients.
   * @return auxiliary alert group info, including a list of auxiliary email recipients (separated
   *     by commas).
   */
  AuxiliaryAlertGroupInfo getAlertGroupAuxiliaryInfo(DimensionMap dimensions,
      List<MergedAnomalyResultDTO> anomalyResultList);
}
