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
package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import java.sql.Timestamp;

public class DataSourceWrapper {
  private final ThirdEyeDataSource dataSource;
  private final Timestamp updateTime;

  public DataSourceWrapper(final ThirdEyeDataSource dataSource, final Timestamp updateTime) {
    this.dataSource = dataSource;
    this.updateTime = updateTime;
  }

  public ThirdEyeDataSource getDataSource() {
    return dataSource;
  }

  public Timestamp getUpdateTime() {
    return updateTime;
  }
}
