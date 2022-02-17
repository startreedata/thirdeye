/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.RelationalQuery;

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
