/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;

/**
 * Simple dataclass for AnomalyDetectorResult.
 * Does not perform protective copies.
 * */
public class SimpleAnomalyDetectorResult implements AnomalyDetectorResult {

  private final DataFrame dataFrame;

  public SimpleAnomalyDetectorResult(
      final DataFrame dataFrame) {
    this.dataFrame = dataFrame;
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }
}
