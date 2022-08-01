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
package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detectionpipeline.operator.sql.DataTableToSqlAdapterFactory;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlExecutionOperator extends DetectionPipelineOperator {

  private static final Logger LOG = LoggerFactory.getLogger(SqlExecutionOperator.class);
  private static final String SQL_ENGINE = "sql.engine";
  private static final String SQL_QUERIES = "sql.queries";
  /**
   * User can pass jdbc parameters.
   * In Map<String, String> format.
   * See https://calcite.apache.org/docs/adapter.html#jdbc-connect-string-parameters
   */
  private static final String JDBC_CONNECTION_PARAMS = "jdbc.parameters";
  private static final String DEFAULT_SQL_ENGINE = "HYPERSQL";

  private final List<String> queries = new ArrayList<>();
  private DataTableToSqlAdapter dataTableToSqlAdapter;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    for (final OutputBean outputBean : context.getPlanNode().getOutputs()) {
      outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
    }
    if (planNode.getParams().containsKey(SQL_QUERIES)) {
      queries.addAll((List<String>) planNode.getParams().get(SQL_QUERIES).value());
    } else {
      throw new IllegalArgumentException(
          "Missing property '" + SQL_QUERIES + "' in SqlExecutionOperator");
    }

    dataTableToSqlAdapter = DataTableToSqlAdapterFactory.create(
        optional(planNode.getParams().get(SQL_ENGINE)).map(Templatable::value)
            .orElse(DEFAULT_SQL_ENGINE)
            .toString());
    if (planNode.getParams().containsKey(JDBC_CONNECTION_PARAMS)) {
      dataTableToSqlAdapter.jdbcProperties()
          .putAll((Map<String, String>) planNode.getParams().get(JDBC_CONNECTION_PARAMS).value());
    }
  }

  @Override
  public final void execute() throws Exception {
    Connection connection = getConnection();
    initTables(connection);
    runQueries(connection);
    dataTableToSqlAdapter.tearDown(connection);
  }

  private Connection getConnection() throws ClassNotFoundException, SQLException {
    try {
      Class.forName(dataTableToSqlAdapter.jdbcDriverClassName());
    } catch (final Exception e) {
      LOG.error("ERROR: failed to load JDBC driver class {}.",
          dataTableToSqlAdapter.jdbcDriverClassName(),
          e);
      throw e;
    }
    final Connection connection = DriverManager.getConnection(dataTableToSqlAdapter.jdbcConnection(),
        dataTableToSqlAdapter.jdbcProperties());
    LOG.debug("Successfully connected to JDBC connection: {} with driver class: {} ",
        dataTableToSqlAdapter.jdbcConnection(),
        dataTableToSqlAdapter.jdbcDriverClassName());

    return connection;
  }

  private void initTables(final Connection connection) throws SQLException {
    Map<String, DataTable> datatables = new HashMap<>();
    for (String tableName : inputMap.keySet()) {
      DetectionPipelineResult detectionPipelineResult = inputMap.get(tableName);
      if (detectionPipelineResult instanceof DataTable) {
        datatables.put(tableName, (DataTable) detectionPipelineResult);
      }
    }
    try {
      dataTableToSqlAdapter.loadTables(connection, datatables);
    } catch (final SQLException e) {
      LOG.error("Failed to load tables");
      throw e;
    }
  }

  private void runQueries(final Connection connection) throws SQLException {
    int i = 0;
    for (final String query : queries) {
      try {
        DataTable dataTable = runQuery(query, connection);
        setOutput(Integer.toString(i++), dataTable);
      } catch (final SQLException e) {
        LOG.error("Got exceptions when executing SQL query: {}", query, e);
        throw e;
      }
    }
  }

  private DataTable runQuery(final String query, final Connection connection) throws SQLException {
    final Statement stmt = connection.createStatement();
    final ResultSet resultSet = stmt.executeQuery(query);
    return ThirdEyeUtils.getDataTableFromResultSet(resultSet);
  }

  @Override
  public String getOperatorName() {
    return "SqlExecutionOperator";
  }
}
