/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

public class DatasetStatsApi implements ThirdEyeApi {

  private DatasetApi dataset;

  private Long nRows;

  public DatasetApi getDataset() {
    return dataset;
  }

  public DatasetStatsApi setDataset(final DatasetApi dataset) {
    this.dataset = dataset;
    return this;
  }

  public Long getnRows() {
    return nRows;
  }

  public DatasetStatsApi setnRows(final Long nRows) {
    this.nRows = nRows;
    return this;
  }
}
