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
package ai.startree.thirdeye.detectionpipeline.sql.macro;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.queryToNode;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.detectionpipeline.sql.macro.function.TimeFilterFunction;
import ai.startree.thirdeye.detectionpipeline.sql.macro.function.TimeGroupFunction;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacroEngine {

  private static final Logger LOG = LoggerFactory.getLogger(MacroEngine.class);
  private final static List<MacroFunction> CORE_MACROS = ImmutableList.of(
      new TimeFilterFunction(),
      new TimeGroupFunction()
  );
  public static final boolean QUOTE_IDENTIFIERS = false;

  private final SqlParser.Config sqlParserConfig;
  private final SqlDialect sqlDialect;
  private final String tableName;
  private final String query;
  private final Map<String, String> properties;
  private final MacroFunctionContext macroFunctionContext;
  private final Map<String, MacroFunction> availableMacros = new HashMap<>();

  public MacroEngine(final SqlLanguage sqlLanguage, final SqlExpressionBuilder sqlExpressionBuilder,
      final Interval detectionInterval,
      @Nullable final DatasetConfigDTO datasetConfigDTO, String query) {
    this.sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    this.sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    this.tableName = optional(datasetConfigDTO).map(DatasetConfigDTO::getDataset).orElse(null);
    this.query = query;
    this.properties = new HashMap<>();
    this.macroFunctionContext = new MacroFunctionContext()
        .setSqlExpressionBuilder(sqlExpressionBuilder)
        .setDetectionInterval(detectionInterval)
        .setDatasetConfigDTO(datasetConfigDTO)
        .setLiteralUnquoter(this.sqlDialect::unquoteStringLiteral)
        .setIdentifierQuoter(this.sqlDialect::quoteIdentifier)
        .setProperties(this.properties);
    // possible to put datasource-specific macros here in the future
    for (MacroFunction function : CORE_MACROS) {
      this.availableMacros.put(function.name(), function);
    }
  }

  public DataSourceRequest prepareRequest() throws SqlParseException {
    SqlNode rootNode = queryToNode(query, sqlParserConfig);
    SqlNode appliedMacrosNode = applyMacros(rootNode);
    String preparedQuery = nodeToQuery(appliedMacrosNode, sqlDialect, QUOTE_IDENTIFIERS);

    return new DataSourceRequest(tableName, preparedQuery, properties);
  }

  private SqlNode applyMacros(SqlNode rootNode) {
    return rootNode.accept(new MacroVisitor());
  }

  private List<String> paramsFromCall(final SqlCall call) {
    return call.getOperandList().stream()
        // don't quote identifiers to make parsing simpler - but datasource sql expression generators have to manage quoting downstream
        .map(n -> nodeToQuery(n, sqlDialect, false))
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
      final MacroFunction macroFunction = availableMacros.get(call.getOperator().getName());
      if (macroFunction != null) {
        List<String> macroParams = paramsFromCall(call);
        String expandedMacro = macroFunction.expandMacro(macroParams, macroFunctionContext);
        try {
          return expressionToNode(expandedMacro, sqlParserConfig);
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
