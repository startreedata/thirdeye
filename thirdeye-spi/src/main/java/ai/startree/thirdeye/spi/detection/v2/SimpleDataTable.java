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
package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.dataframe.DataFrame;

public class SimpleDataTable extends AbstractDataTableImpl {
  private final DataFrame dataFrame;

  private SimpleDataTable(final DataFrame dataFrame) {
    this.dataFrame = dataFrame;
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }

  /**does not perform a copy*/
  public static DataTable fromDataFrame(final DataFrame dataFrame) {
    return new SimpleDataTable(dataFrame);
  }
}
