package org.apache.pinot.thirdeye.detection.v2.sql.macro;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.pinot.thirdeye.detection.v2.sql.SqlLanguageTranslator;
import org.apache.pinot.thirdeye.detection.v2.sql.macro.function.TimeFilterFunction;
import org.apache.pinot.thirdeye.detection.v2.sql.macro.function.TimeGroupFunction;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunction;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionContext;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlLanguage;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacroEngine {

  private static final Logger LOG = LoggerFactory.getLogger(MacroEngine.class);
  private final static List<MacroFunction> CORE_MACROS = ImmutableList.of(
      new TimeFilterFunction(),
      new TimeGroupFunction()
  );

  private final SqlParser.Config sqlParserConfig;
  private final SqlDialect sqlDialect;
  private final String tableName;
  private final String query;
  private final Map<String, String> properties;
  private final MacroFunctionContext macroFunctionContext;
  private final Map<String, MacroFunction> availableMacros = new HashMap<>();

  public MacroEngine(final SqlLanguage sqlLanguage, final SqlExpressionBuilder sqlExpressionBuilder,
      final Interval detectionInterval,
      String tableName, String query) {
    this.sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    this.sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    this.tableName = tableName;
    this.query = query;
    this.properties = new HashMap<>();
    this.macroFunctionContext = new MacroFunctionContext()
        .setSqlExpressionBuilder(sqlExpressionBuilder)
        .setDetectionInterval(detectionInterval)
        .setLiteralUnquoter(this.sqlDialect::unquoteStringLiteral)
        .setProperties(this.properties);
    // possible to put datasource-specific macros here in the future
    for (MacroFunction function: CORE_MACROS )
      this.availableMacros.put(function.name(), function);
  }

  public ThirdEyeRequestV2 prepareRequest() throws SqlParseException {
    SqlNode rootNode = queryToNode(query);
    SqlNode appliedMacrosNode = applyMacros(rootNode);
    String preparedQuery = nodeToQuery(appliedMacrosNode);

    return new ThirdEyeRequestV2(tableName, preparedQuery, properties);
  }

  private SqlNode queryToNode(final String sql) throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sql, sqlParserConfig);
    return sqlParser.parseQuery();
  }

  private SqlNode expressionToNode(final String sqlExpression) throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sqlExpression,
        sqlParserConfig);
    return sqlParser.parseExpression();
  }

  private SqlNode applyMacros(SqlNode rootNode) {
    return rootNode.accept(new MacroVisitor());
  }

  private String nodeToQuery(final SqlNode node) {
    return node.toSqlString(
        c -> c.withDialect(sqlDialect)
            .withQuoteAllIdentifiers(false)
    ).getSql();
  }

  private List<String> paramsFromCall(final SqlCall call) {
    return call.getOperandList().stream()
        .map(this::nodeToQuery)
        .collect(Collectors.toList());
  }

  private class MacroVisitor extends SqlShuttle {

    @Override
    public @Nullable
    SqlNode visit(SqlCall call) {
      // depth-first traverse and replace macros
      CallCopyingArgHandler argHandler = new CallCopyingArgHandler(call, false);
      call.getOperator().acceptCall(this, call, false, argHandler);

      return replaceIfMacro((SqlCall) argHandler.result());
    }

    private SqlNode replaceIfMacro(SqlCall call) {
      if (call.getOperator().getKind() != SqlKind.OTHER_FUNCTION) {
        // cannot be a macro function
        return call;
      }
      MacroFunction macroFunction = availableMacros.get(call.getOperator().getName());
      if (macroFunction != null) {
        List<String> macroParams = paramsFromCall(call);
        String expandedMacro = macroFunction.expandMacro(macroParams, macroFunctionContext);
        try {
          return expressionToNode(expandedMacro);
        } catch (SqlParseException e) {
          LOG.error(String.format("Failed parsing expanded macro into a SQL node: %s. %s",
              expandedMacro,
              e));
        }
      }
      // not a macro OR macro expansion parsing failed: return input unchanged
      return call;
    }
  }
}
