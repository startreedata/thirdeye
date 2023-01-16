/*
 * Copyright 2023 StarTree Inc
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
 * Subset of Calcite SqlParser.Config configuration.
 * Makes spi not dependent on Calcite.
 * This class is translated to a Calcite object in thirdeye-core.
 * See examples in MacroEngineTest.TestSqlLanguage and PinotSqlLanguage.
 * */
public class ThirdEyeSqlParserConfig {
  private final String lex;
  private final String conformance;
  private final String parserFactory;
  private final String quoting;
  private final String quotedCasing;
  private final String unquotedCasing;

  private ThirdEyeSqlParserConfig(final String lex, final String conformance,
      final String parserFactory, final String quoting, final String quotedCasing,
      final String unquotedCasing) {
    this.lex = lex;
    this.conformance = conformance;
    this.parserFactory = parserFactory;
    this.quoting = quoting;
    this.quotedCasing = quotedCasing;
    this.unquotedCasing = unquotedCasing;
  }

  public String getLex() {
    return lex;
  }

  public String getConformance() {
    return conformance;
  }

  public String getParserFactory() {
    return parserFactory;
  }

  public String getQuoting() {
    return quoting;
  }

  public String getQuotedCasing() {
    return quotedCasing;
  }

  public String getUnquotedCasing() {
    return unquotedCasing;
  }

  public static class Builder {

    private String lex;
    private String conformance;
    private String parserFactory;
    private String quoting;
    private String quotedCasing;
    private String unquotedCasing;

    public Builder withLex(final String lex) {
      this.lex = lex;
      return this;
    }

    public Builder withConformance(final String conformance) {
      this.conformance = conformance;
      return this;
    }

    public Builder withParserFactory(final String parserFactory) {
      this.parserFactory = parserFactory;
      return this;
    }

    public Builder withQuoting(final String quoting) {
      this.quoting = quoting;
      return this;
    }

    public Builder withQuotedCasing(final String quotedCasing) {
      this.quotedCasing = quotedCasing;
      return this;
    }

    public Builder withUnquotedCasing(final String unquotedCasing) {
      this.unquotedCasing = unquotedCasing;
      return this;
    }

    public ThirdEyeSqlParserConfig build() {
      return new ThirdEyeSqlParserConfig(lex,
          conformance,
          parserFactory,
          quoting,
          quotedCasing,
          unquotedCasing);
    }
  }
}
