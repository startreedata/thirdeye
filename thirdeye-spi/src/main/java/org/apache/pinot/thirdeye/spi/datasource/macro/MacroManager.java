package org.apache.pinot.thirdeye.spi.datasource.macro;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.joda.time.Period;

/**
 * A provider of SQL parsers and SQL generation functions for a data source.
 * Used to resolve macro functions.
 */
public interface MacroManager {

  /**
   * See pinot datasource implementation for reference.
   * */
  SqlParser.Config getSqlParserConfig();

  /**
   * @see <a href="https://github.com/apache/calcite/tree/master/core/src/main/java/org/apache/calcite/sql/dialect">Calcite dialect doc</a>
   * */
  SqlDialect getSqlDialect();

  /**
   * Generates a SQL expression that test if a time column in milliseconds is between bounds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: {@code getTimeFilterExpression(myTimeEpoch, 42, 84)} returns "myTimeEpoch >= 42 AND myTimeEpoch < 84"
   * @param column epoch milliseconds column name
   * @param minTimeMillisIncluded minimum epoch milliseconds - included
   * @param maxTimeMillisExcluded maximum epoch milliseconds - included
   */
  default String getTimeFilterExpression(String column, long minTimeMillisIncluded,
      long maxTimeMillisExcluded) {
    return String.format("%s >= %s AND %s < %s",
        column,
        minTimeMillisIncluded,
        column,
        maxTimeMillisExcluded);
  }

  /**
   * Generate a SQL expression that rounds to a given granularity and cast to epoch milliseconds.
   * Does not escape identifiers. Identifiers that have to be escaped should be passed escaped.
   * For example: for a MySQL implementation, {@code getTimeGroupExpression(dateTimeColumn, DATETIME, P1D)} returns "UNIX_TIMESTAMP(DATE(dateTimeColumn))*1000"
   * @param timeColumn time column name
   * @param timeColumnFormat time column time format. Managed formats depend on the datasource
   * @param granularity granularity of the output epoch milliseconds (minutes, days, etc...)
   */
  default String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
      Period granularity) {
    throw new UnsupportedOperationException();
  }
}
