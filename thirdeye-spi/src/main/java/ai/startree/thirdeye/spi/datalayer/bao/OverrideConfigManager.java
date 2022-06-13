/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import java.util.List;

public interface OverrideConfigManager extends AbstractManager<OverrideConfigDTO> {

  String TARGET_COLLECTION = "collection";
  String TARGET_METRIC = "metric";
  String TARGET_FUNCTION_ID = "functionId";
  String[] TARGET_KEYS =
      new String[]{TARGET_COLLECTION, TARGET_METRIC, TARGET_FUNCTION_ID};
  String EXCLUDED_COLLECTION = "excludedCollection";
  String EXCLUDED_METRIC = "excludedMetric";
  String EXCLUDED_FUNCTION_ID = "excludedFunctionId";
  String[] EXCLUDED_KEYS =
          new String[]{EXCLUDED_COLLECTION, EXCLUDED_METRIC, EXCLUDED_FUNCTION_ID};
  String ENTITY_TIME_SERIES = "TimeSeries";
  String ENTITY_ALERT_FILTER = "AlertFilter";

  List<OverrideConfigDTO> findAllConflictByTargetType(String typeName, long windowStart,
      long windowEnd);
}
