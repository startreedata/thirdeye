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
package ai.startree.thirdeye.plugins.datasource.pinotsql;

import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import com.google.common.base.MoreObjects;

public class PinotSqlQuery extends RelationalQuery {

  private String tableName;

  public PinotSqlQuery(String query, String tableName) {
    super(query);
    this.tableName = tableName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("query", query)
        .add("tableName", tableName)
        .toString();
  }
}
