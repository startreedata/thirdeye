/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * A provider of SQL expression generation methods.
 * Used to expand macro functions into valid SQL.
 */
public interface SqlExpressionBuilder {

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   * Implementation should be careful with identifier quoting.
   * Identifiers may be passed quoted or unquoted.
   *
   * For example: {@code getTimeFilterExpression(myTimeEpoch, 42, 84, null)} returns
   * "myTimeEpoch >= 42 AND myTimeEpoch < 84"
   *
   * Used by the macro __timeFilter().
   * Expects the column to be in epoch milliseconds. //fixme cyril add more choice
   *
   * @param timeColumn time column name
   * @param filterInterval interval to filter on. // fixme cyril explain timezone
   */
  default String getTimeFilterExpression(final String timeColumn, final Interval filterInterval) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generates a SQL expression that test if a time column is between bounds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   *
   * Used internally to generate queries. For RCA for instance.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the
   * datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the format is epoch milliseconds.
   *
   * This method can either generate the datetime in the given format directly,
   * or generate a SQL that transforms the colum or the milliseconds in the correct format at
   * runtime.
   *
   * @param timeColumn time column name
   * @param filterInterval interval to filter on. // fixme cyril explain timezone
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free
   *     to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   */
  default String getTimeFilterExpression(final String timeColumn, final Interval filterInterval, final @Nullable String timeFormat,
      @Nullable final String timeUnit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Round with the given timezone. If timezone is null, keep the default timezone
   * behavior of the datasource.
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
      final Period granularity, @Nullable final String timezone) {
    throw new UnsupportedOperationException();
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * Round with the given timezone. If timezone is null, keep the default timezone
   * behavior of the datasource.
   *
   * timeFormat comes from DatasetConfigDTO$format, so it can be anything. It is specific to the
   * datasource.
   * The function should be able to parse the timeFormat used by the datasource.
   * If it's null, the function should assume the input format is epoch milliseconds.
   *
   * Used internally to generate queries. For RCA for instance.
   *
   * @param timeFormat any string, coming from DatasetConfigDTO$format - the datasource is free
   *     to put any format in DatasetConfigDTO$format.
   * @param timeUnit the String of a TimeUnit, coming from DatasetConfigDTO$timeUnit.
   */
  default String getTimeGroupExpression(final String timeColumn, @Nullable final String timeFormat,
      final Period granularity, @Nullable final String timeUnit , @Nullable final String timezone) {
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

  /**
   * Generate a SQL expression for the given metric aggregation function, with the given operands.
   */
  default String getCustomDialectSql(final MetricAggFunction metricAggFunction,
      final List<String> operands,
      String quantifier) {
    throw new UnsupportedOperationException();
  }
}
