/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import java.util.List;

public interface DetectionStatusManager extends AbstractManager<DetectionStatusDTO> {

  DetectionStatusDTO findLatestEntryForFunctionId(long functionId);

  List<DetectionStatusDTO> findAllInTimeRangeForFunctionAndDetectionRun(long startTime,
      long endTime, long functionId,
      boolean detectionRun);
}
