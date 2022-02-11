/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import java.util.List;

public interface DetectionPipelineResult {

  List<DetectionResult> getDetectionResults();

  default long getLastTimestamp() {
    return -1;
  }

  default List<MergedAnomalyResultDTO> getAnomalies() {
    throw new UnsupportedOperationException();
  }

  default List<EvaluationDTO> getEvaluations() {
    throw new UnsupportedOperationException();
  }
}
