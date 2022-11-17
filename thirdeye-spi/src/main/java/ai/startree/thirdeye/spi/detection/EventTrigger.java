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
package ai.startree.thirdeye.spi.detection;

import java.util.List;

public interface EventTrigger<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Initialize Trigger
   */
  void init(T spec);

  /**
   * Trigger with one event
   */
  void trigger(List<String> columnNames, Object[] event) throws EventTriggerException;

  /**
   * Close the Trigger
   */
  void close();
}
