/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
