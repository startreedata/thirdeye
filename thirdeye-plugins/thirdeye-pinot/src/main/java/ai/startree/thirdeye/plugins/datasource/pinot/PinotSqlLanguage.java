/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.datasource.pinot;

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
