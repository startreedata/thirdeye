/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;

public class PinotSqlLanguage implements SqlLanguage {

  private static final ThirdEyeSqlParserConfig SQL_PARSER_CONFIG = new ThirdEyeSqlParserConfig.Builder()
      .withLex("MYSQL_ANSI")
      .withConformance("BABEL")
      .withParserFactory("SqlBabelParserImpl")
      .build();

  // todo cyril this dialect may be incomplete
  private static final ThirdeyeSqlDialect SQL_DIALECT = new ThirdeyeSqlDialect.Builder()
      .withBaseDialect("AnsiSqlDialect")
      .withIdentifierQuoteString("\"")
      .build();

  @Override
  public ThirdEyeSqlParserConfig getSqlParserConfig() {
    return SQL_PARSER_CONFIG;
  }

  @Override
  public ThirdeyeSqlDialect getSqlDialect() {
    return SQL_DIALECT;
  }
}
