/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
