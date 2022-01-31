package org.apache.pinot.thirdeye.detection.v2.sql;

import java.lang.reflect.Field;
import java.util.Objects;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialect.Context;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.babel.SqlBabelParserImpl;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.pinot.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import org.apache.pinot.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;

/**
 * Utils to translate thirdeye-spi SqlLanguage objects into Calcite objects.
 */
public class SqlLanguageTranslator {

  public static SqlParser.Config translate(ThirdEyeSqlParserConfig config) {
    SqlParser.Config calciteConfig = SqlParser.config();
    if (config.getLex() != null) {
      calciteConfig = calciteConfig.withLex(Lex.valueOf(config.getLex()));
    }
    if (config.getConformance() != null) {
      calciteConfig = calciteConfig.withConformance(SqlConformanceEnum.valueOf(config.getConformance()));
    }
    if (config.getParserFactory() != null) {
      switch (config.getParserFactory()) {
        case "SqlBabelParserImpl":
          calciteConfig = calciteConfig.withParserFactory(SqlBabelParserImpl.FACTORY);
          break;
        case "SqlParserImpl":
          calciteConfig = calciteConfig.withParserFactory(SqlParserImpl.FACTORY);
          break;
        default:
          throw new IllegalArgumentException(String.format(
              "ParserFactory equivalence not implemented for: %s",
              config.getParserFactory()));
      }
    }
    if (config.getQuoting() != null) {
      calciteConfig = calciteConfig.withQuoting(Quoting.valueOf(config.getQuoting()));
    }
    if (config.getQuotedCasing() != null) {
      calciteConfig = calciteConfig.withQuotedCasing(Casing.valueOf(config.getQuotedCasing()));
    }
    if (config.getUnquotedCasing() != null) {
      calciteConfig = calciteConfig.withUnquotedCasing(Casing.valueOf(config.getUnquotedCasing()));
    }

    return calciteConfig;
  }

  public static SqlDialect translate(ThirdeyeSqlDialect dialect) {
    String baseDialect = dialect.getBaseDialect();
    Objects.requireNonNull(baseDialect, "Base dialect cannot be null.");
    // get context from dialect name
    Context context;
    try {
      Class<?> dialectClass = Class.forName("org.apache.calcite.sql.dialect." + baseDialect);
      Field contextField = dialectClass.getDeclaredField("DEFAULT_CONTEXT");
      context = (Context) contextField.get(dialectClass);
    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException(
          String.format("Could not build a Calcite dialect from dialect name: %s", baseDialect));
    }

    // enrich context
    if (dialect.getCaseSensitive() != null) {
      context = context.withCaseSensitive(dialect.getCaseSensitive());
    }
    if (dialect.getIdentifierQuoteString() != null) {
      context = context.withIdentifierQuoteString(dialect.getIdentifierQuoteString());
    }
    if (dialect.getIdentifierEscapedQuoteString() != null) {
      context = context.withIdentifierEscapedQuoteString(dialect.getIdentifierEscapedQuoteString());
    }
    if (dialect.getLiteralEscapedQuoteString() != null) {
      context = context.withLiteralEscapedQuoteString(dialect.getLiteralEscapedQuoteString());
    }
    if (dialect.getLiteralQuoteString() != null) {
      context = context.withLiteralQuoteString(dialect.getLiteralQuoteString());
    }
    if (dialect.getQuotedCasing() != null) {
      context = context.withQuotedCasing(Casing.valueOf(dialect.getQuotedCasing()));
    }
    if (dialect.getUnquotedCasing() != null) {
      context = context.withUnquotedCasing(Casing.valueOf(dialect.getUnquotedCasing()));
    }

    return new SqlDialect(context);
  }
}
