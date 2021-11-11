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
  private static final PinotSqlLanguage INSTANCE = new PinotSqlLanguage();

  private PinotSqlLanguage(){}

  public static PinotSqlLanguage getInstance(){
    return INSTANCE;
  }

  @Override
  public Config getSqlParserConfig() {
    return SqlParser.config()
        .withLex(Lex.MYSQL_ANSI)
        .withConformance(SqlConformanceEnum.BABEL)
        .withParserFactory(SqlBabelParserImpl.FACTORY);
  }

  @Override
  public SqlDialect getSqlDialect() {
    // fixme cyril not sure this dialect is correct - contribute fully correct dialect to calcite ?
    return new SqlDialect(AnsiSqlDialect.DEFAULT_CONTEXT.withIdentifierQuoteString("\""));
  }
}
