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
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DetectionPipelineUtils {

  public static Map<String, DataTable> getDataTableMap(
      final Map<String, OperatorResult> inputMap) {
    final Map<String, DataTable> dataTableMap = new HashMap<>();
    for (final Entry<String, OperatorResult> entry : inputMap.entrySet()) {
      if (entry.getValue() instanceof DataTable) {
        dataTableMap.put(entry.getKey(), (DataTable) entry.getValue());
      }
    }
    return dataTableMap;
  }
}
