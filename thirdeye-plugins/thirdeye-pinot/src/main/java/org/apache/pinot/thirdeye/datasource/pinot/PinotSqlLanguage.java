package org.apache.pinot.thirdeye.datasource.pinot;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.babel.SqlBabelParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlLanguage;

public class PinotSqlLanguage implements SqlLanguage {

  private static final Config SQL_PARSER_CONFIG = SqlParser.config()
      .withLex(Lex.MYSQL_ANSI)
        .withConformance(SqlConformanceEnum.BABEL)
        .withParserFactory(SqlBabelParserImpl.FACTORY);

  // todo cyril this dialect may be incomplete
  private static final SqlDialect SQL_DIALECT = new SqlDialect(AnsiSqlDialect.DEFAULT_CONTEXT.withIdentifierQuoteString("\""));

  @Override
  public Config getSqlParserConfig() {
    return SQL_PARSER_CONFIG;
  }

  @Override
  public SqlDialect getSqlDialect() {
    return SQL_DIALECT;
  }
}
