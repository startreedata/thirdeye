/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.OnlineDetectionDataDTO;
import java.util.List;

public interface OnlineDetectionDataManager extends AbstractManager<OnlineDetectionDataDTO> {

  List<OnlineDetectionDataDTO> findByDatasetAndMetric(String dataset, String metric);
}
