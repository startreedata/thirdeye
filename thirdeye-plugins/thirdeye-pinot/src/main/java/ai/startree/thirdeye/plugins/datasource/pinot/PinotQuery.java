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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import java.util.Map;

// TODO CYRIL - remove ? duplication of the DataSourceRequest
public class PinotQuery extends RelationalQuery {

  private String tableName;

  private final Map<String, String> options;

  @Deprecated // TODO CYRIL always true - to remove
  private final boolean useSql = true;

  public PinotQuery(final String query, final String tableName, final Map<String, String> options) {
    super(query);
    this.tableName = tableName;
    this.options = options;
  }

  public String getTableName() {
    return tableName;
  }

  public boolean isUseSql() {
    return useSql;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PinotQuery{");
    sb.append("query='").append(query).append('\'');
    sb.append(", tableName='").append(tableName).append('\'');
    sb.append(", useSql='").append(useSql).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
