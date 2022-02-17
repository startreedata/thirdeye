/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

public abstract class RelationalQuery {

  protected String query;

  public RelationalQuery(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public int hashCode() {
    return query.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    RelationalQuery that = (RelationalQuery) obj;
    return this.query.equals(that.query);
  }
}

