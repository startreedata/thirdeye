package org.apache.pinot.thirdeye.spi.datasource.macro;

/**
 * A provider of Calcite SQL parsers SQL dialect for a data source.
 * Used to parse and generate SQL when using macro functions.
 */
public interface SqlLanguage {

  /**
   * See pinot datasource implementation for reference.
   * The object returned will be cast to a Calcite SqlParser.Config.
   */
  Object getSqlParserConfig();

  /**
   * @see <a href="https://github.com/apache/calcite/tree/master/core/src/main/java/org/apache/calcite/sql/dialect">Calcite
   *     dialect doc</a>.
   * See pinot datasource implementation for reference.
   * The object returned will be cast to a Calcite SqlDialect.
   */
  Object getSqlDialect();
}
