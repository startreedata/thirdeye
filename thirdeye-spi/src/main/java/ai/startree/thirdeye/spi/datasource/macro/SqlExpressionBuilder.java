/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Period;

/**
 * A provider of SQL expression generation methods.
 * Used to expand macro functions into valid SQL.
 */
public interface SqlExpressionBuilder {

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: {@code getTimeFilterExpression(myTimeEpoch, 42, 84, null)} returns
   * "myTimeEpoch >= 42 AND myTimeEpoch < 84"
   *
   * Used by the macro __timeFilter().
   * Expects the column to be in epoch milliseconds.
   *
   * @param timeColumn time column name
   * @param minTimeMillisIncluded minimum epoch milliseconds - included
   * @param maxTimeMillisExcluded maximum epoch milliseconds - excluded
   */
  default String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
      final long maxTimeMillisExcluded) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   *
   * Used internally to generate queries. For RCA for instance.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the format is epoch milliseconds.
   *
   * This method can either generate the datetime in the given format directly,
   * or generate a SQL that transforms the colum or the milliseconds in the correct format at
   * runtime.
   *
   * @param timeColumn time column name
   * @param minTimeMillisIncluded minimum epoch milliseconds - included
   * @param maxTimeMillisExcluded maximum epoch milliseconds - excluded
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   */
  default String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
      final long maxTimeMillisExcluded, final @Nullable String timeFormat,
      @Nullable final String timeUnit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: for a MySQL implementation, {@code getTimeGroupExpression(dateTimeColumn,
   * DATETIME, P1D)} returns "UNIX_TIMESTAMP(DATE(dateTimeColumn))*1000"
   *
   * Used by macro __timeGroup()
   *
   * @param timeColumn time column name
   * @param timeColumnFormat time column time format. Managed formats depend on the datasource
   * @param granularity granularity of the output epoch milliseconds (minutes, days, etc...)
   */
  default String getTimeGroupExpression(final String timeColumn, final String timeColumnFormat,
      final Period granularity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the input format is epoch milliseconds.
   *
   * Used internally to generate queries. For RCA for instance.
   *
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   * */
  default String getTimeGroupExpression(final String timeColumn, @Nullable final String timeFormat,
      final Period granularity, @Nullable final String timeUnit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns whether a MetricAggFunction requires a custom SQL statement or
   * can be safely generated with ANSI SQL.
   */
  default boolean needsCustomDialect(final MetricAggFunction metricAggFunction) {
    switch (metricAggFunction) {
      case PCT50:
      case PCT90:
      case PCT95:
      case PCT99:
        return true;
      default:
        return false;
    }
  }

  default String getCustomDialectSql(final MetricAggFunction metricAggFunction,
      final List<String> operands,
      String quantifier) {
    throw new UnsupportedOperationException();
  }
}
