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
package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import java.util.Objects;

/**
 * Request object containing all information for a {@link ThirdEyeDataSource} to retrieve {@link
 * DataTable}.
 */
public class DataSourceRequest {

  private final String table;
  private final String query;
  private final Map<String, String> properties;

  public DataSourceRequest(final String table,
      final String query,
      final Map<String, String> properties) {
    this.table = table;
    this.query = query;
    this.properties = properties;
  }

  public String getTable() {
    return table;
  }

  public String getQuery() {
    return query;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataSourceRequest that = (DataSourceRequest) o;
    return Objects.equals(table, that.table)
        && Objects.equals(query, that.query)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(table, query, properties);
  }

  @Override
  public String toString() {
    return "DataSourceRequest{" +
        "table='" + table + '\'' +
        ", query='" + query + '\'' +
        ", properties=" + properties +
        '}';
  }
}
