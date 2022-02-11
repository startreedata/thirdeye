/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

/**
 * Subset of Calcite ThirdeyeSqlDialect configuration.
 * Makes spi not dependent on Calcite.
 * This class is translated to a Calcite object in thirdeye-core.
 * See examples in MacroEngineTest.TestSqlLanguage and PinotSqlLanguage.
 * */
public class ThirdeyeSqlDialect {

  /**
   * something in org.apache.calcite.sql.dialect, for example AnsiSqlDialect
   */
  private final String baseDialect;
  private final Boolean caseSensitive;
  private final String identifierQuoteString;
  private final String identifierEscapedQuoteString;
  private final String literalEscapedQuoteString;
  private final String literalQuoteString;
  private final String quotedCasing;
  private final String unquotedCasing;

  public ThirdeyeSqlDialect(final String baseDialect, final Boolean caseSensitive,
      final String identifierQuoteString, final String identifierEscapedQuoteString,
      final String literalEscapedQuoteString, final String literalQuoteString,
      final String quotedCasing,
      final String unquotedCasing) {
    this.baseDialect = baseDialect;
    this.caseSensitive = caseSensitive;
    this.identifierQuoteString = identifierQuoteString;
    this.identifierEscapedQuoteString = identifierEscapedQuoteString;
    this.literalEscapedQuoteString = literalEscapedQuoteString;
    this.literalQuoteString = literalQuoteString;
    this.quotedCasing = quotedCasing;
    this.unquotedCasing = unquotedCasing;
  }

  public String getBaseDialect() {
    return baseDialect;
  }

  public Boolean getCaseSensitive() {
    return caseSensitive;
  }

  public String getIdentifierQuoteString() {
    return identifierQuoteString;
  }

  public String getIdentifierEscapedQuoteString() {
    return identifierEscapedQuoteString;
  }

  public String getLiteralEscapedQuoteString() {
    return literalEscapedQuoteString;
  }

  public String getLiteralQuoteString() {
    return literalQuoteString;
  }

  public String getQuotedCasing() {
    return quotedCasing;
  }

  public String getUnquotedCasing() {
    return unquotedCasing;
  }

  public static class Builder {

    private String baseDialect;
    private Boolean caseSensitive;
    private String identifierQuoteString;
    private String identifierEscapedQuoteString;
    private String literalEscapedQuoteString;
    private String literalQuoteString;
    private String quotedCasing;
    private String unquotedCasing;

    public Builder withBaseDialect(final String baseDialect) {
      this.baseDialect = baseDialect;
      return this;
    }

    public Builder withCaseSensitive(final Boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
      return this;
    }

    public Builder withIdentifierQuoteString(final String identifierQuoteString) {
      this.identifierQuoteString = identifierQuoteString;
      return this;
    }

    public Builder withIdentifierEscapedQuoteString(
        final String identifierEscapedQuoteString) {
      this.identifierEscapedQuoteString = identifierEscapedQuoteString;
      return this;
    }

    public Builder withLiteralEscapedQuoteString(
        final String literalEscapedQuoteString) {
      this.literalEscapedQuoteString = literalEscapedQuoteString;
      return this;
    }

    public Builder withLiteralQuoteString(final String literalQuoteString) {
      this.literalQuoteString = literalQuoteString;
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

    public ThirdeyeSqlDialect build() {
      return new ThirdeyeSqlDialect(baseDialect,
          caseSensitive,
          identifierQuoteString,
          identifierEscapedQuoteString,
          literalEscapedQuoteString,
          literalQuoteString,
          quotedCasing,
          unquotedCasing);
    }
  }
}
