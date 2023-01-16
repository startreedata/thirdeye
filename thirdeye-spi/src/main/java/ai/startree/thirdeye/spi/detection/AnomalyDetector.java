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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import org.joda.time.Interval;

public interface AnomalyDetector<T extends AbstractSpec> extends BaseComponent<T> {

  // Keys available in Map<String, DataTable> timeSeriesMap
  // todo cyril move this to Constants
  String KEY_CURRENT = "current";
  String KEY_BASELINE = "baseline";
  String KEY_CURRENT_EVENTS = "current_events";
  String KEY_BASELINE_EVENTS = "baseline_events";

  /**
   * Run detection for a given interval with provided baseline and current DataTable.
   */
  AnomalyDetectorResult runDetection(Interval interval, Map<String, DataTable> dataTableMap)
      throws DetectorException;
}
