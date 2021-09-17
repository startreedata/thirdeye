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

package org.apache.pinot.thirdeye.datasource.pinot;

import org.apache.pinot.thirdeye.spi.datasource.RelationalQuery;

public class PinotQuery extends RelationalQuery {

  private String tableName;

  private boolean useSql = false;

  public PinotQuery(String pql, String tableName) {
    this(pql, tableName, false);
  }

  public PinotQuery(String query, String tableName, boolean useSql) {
    super(query);
    this.tableName = tableName;
    this.useSql = useSql;
  }

  // TODO: Remove thirdeye-external's dependency on this method to getQuery() instead
  public String getPql() {
    return query;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public boolean isUseSql() {
    return useSql;
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
