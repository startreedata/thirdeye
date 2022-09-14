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
package ai.startree.thirdeye.spi.detection.postprocessing;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.Map;
import org.joda.time.Interval;

public interface AnomalyPostProcessor<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Spec class of the PostProcessor.
   */
  Class<T> specClass();

  /**
   * Value to put in AnomalyLabelDTO$sourcePostProcessor field.
   */
  String name();

  /**
   * Run postProcessing operations on a map of results.
   */
  Map<String, OperatorResult> postProcess(final Interval detectionInterval,
      final Map<String, OperatorResult> resultMap)
      throws Exception;
}
