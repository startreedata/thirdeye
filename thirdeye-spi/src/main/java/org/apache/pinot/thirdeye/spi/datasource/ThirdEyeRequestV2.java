/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.datasource;

import java.util.Map;
import java.util.Objects;

/**
 * Request object containing all information for a {@link ThirdEyeDataSource} to retrieve {@link
 * org.apache.pinot.thirdeye.spi.detection.v2.DataTable}.
 */
public class ThirdEyeRequestV2 {

  private final String table;
  private final String query;
  private final Map<String, String> properties;

  public ThirdEyeRequestV2(final String table, final String query,
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
    final ThirdEyeRequestV2 that = (ThirdEyeRequestV2) o;
    return Objects.equals(table, that.table) && Objects.equals(query, that.query)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(table, query, properties);
  }

  @Override
  public String toString() {
    return "ThirdEyeRequestV2{" +
        "table='" + table + '\'' +
        ", query='" + query + '\'' +
        ", properties=" + properties +
        '}';
  }
}
