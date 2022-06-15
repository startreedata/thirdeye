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
package ai.startree.thirdeye.spi.datasource.macro;

/**
 * A provider of Calcite SQL parsers SQL dialect for a data source.
 * Used to parse and generate SQL when using macro functions.
 */
public interface SqlLanguage {

  /**
   * See pinot datasource implementation for reference.
   * The object returned will be cast to a Calcite SqlParser.Config.
   */
  ThirdEyeSqlParserConfig getSqlParserConfig();

  /**
   * @see <a href="https://github.com/apache/calcite/tree/master/core/src/main/java/org/apache/calcite/sql/dialect">Calcite
   *     dialect doc</a>.
   * See pinot datasource implementation for reference.
   * The object returned will be cast to a Calcite SqlDialect.
   */
  ThirdeyeSqlDialect getSqlDialect();
}
