/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

import org.joda.time.Period;

/**
 * A provider of SQL expression generation methods.
 * Used to expand macro functions into valid SQL.
 */
public interface SqlExpressionBuilder {

  /**
   * Generates a SQL expression that test if a time column in milliseconds is between bounds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: {@code getTimeFilterExpression(myTimeEpoch, 42, 84)} returns "myTimeEpoch >= 42
   * AND myTimeEpoch < 84"
   *
   * @param column epoch milliseconds column name
   * @param minTimeMillisIncluded minimum epoch milliseconds - included
   * @param maxTimeMillisExcluded maximum epoch milliseconds - included
   */
  default String getTimeFilterExpression(String column, long minTimeMillisIncluded,
      long maxTimeMillisExcluded) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: for a MySQL implementation, {@code getTimeGroupExpression(dateTimeColumn,
   * DATETIME, P1D)} returns "UNIX_TIMESTAMP(DATE(dateTimeColumn))*1000"
   *
   * @param timeColumn time column name
   * @param timeColumnFormat time column time format. Managed formats depend on the datasource
   * @param granularity granularity of the output epoch milliseconds (minutes, days, etc...)
   */
  default String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
      Period granularity) {
    throw new UnsupportedOperationException();
  }
}
