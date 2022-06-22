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
package ai.startree.thirdeye.plugins.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerException;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.List;

/**
 * Absolute change rule detection
 */
public class ConsoleOutputTrigger implements EventTrigger<ConsoleOutputTriggerSpec> {

  private ConsoleOutputTriggerSpec spec;

  @Override
  public void init(final ConsoleOutputTriggerSpec spec) {
    this.spec = spec;
  }

  @Override
  public void trigger(final List<String> columnNames, final List<ColumnType> columnTypes, final Object[] event) throws EventTriggerException {
    System.out.println(String.format(spec.getFormat(), DataTable.getRecord(columnNames, event)));
  }

  @Override
  public void close() {
  }
}
