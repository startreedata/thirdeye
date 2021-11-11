package org.apache.pinot.thirdeye.spi.datasource.macro;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParser;


/**
 * A provider of Calcite SQL parsers SQL dialect for a data source.
 * Used to parse and generate SQL when using macro functions.
 */
public interface SqlLanguage {
  /**
   * See pinot datasource implementation for reference.
   * */
  SqlParser.Config getSqlParserConfig();

  /**
   * @see <a href="https://github.com/apache/calcite/tree/master/core/src/main/java/org/apache/calcite/sql/dialect">Calcite dialect doc</a>
   * */
  SqlDialect getSqlDialect();
}
