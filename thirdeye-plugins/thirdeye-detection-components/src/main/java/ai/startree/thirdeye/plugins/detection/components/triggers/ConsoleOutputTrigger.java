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
package ai.startree.thirdeye.plugins.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleOutputTrigger implements EventTrigger<ConsoleOutputTriggerSpec> {

  private ConsoleOutputTriggerSpec spec;

  @Override
  public void init(final ConsoleOutputTriggerSpec spec) {
    this.spec = spec;
  }

  @Override
  public void trigger(final List<String> columnNames, final Object[] event) throws EventTriggerException {
    System.out.println(String.format(spec.getFormat(), getRecord(columnNames, event)));
  }

  @Override
  public void close() {
  }

  private static Map<String, Object> getRecord(final List<String> columnNames, final Object[] event) {
    Map<String, Object> record = new HashMap<>();
    for (int i = 0; i < columnNames.size(); i++) {
      record.put(columnNames.get(i), event[i]);
    }
    return record;
  }
}
