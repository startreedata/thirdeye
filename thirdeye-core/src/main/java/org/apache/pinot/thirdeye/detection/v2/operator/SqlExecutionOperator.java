package org.apache.pinot.thirdeye.detection.v2.operator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.sql.DataTableToSqlAdapterFactory;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable.SimpleDataTableBuilder;
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
      queries.addAll((List<String>) planNode.getParams().get(SQL_QUERIES));
    } else {
      throw new IllegalArgumentException(
          "Missing property '" + SQL_QUERIES + "' in SqlExecutionOperator");
    }

    dataTableToSqlAdapter = DataTableToSqlAdapterFactory.create(planNode.getParams()
        .getOrDefault(SQL_ENGINE, DEFAULT_SQL_ENGINE).toString());
    if (planNode.getParams().containsKey(JDBC_CONNECTION_PARAMS)) {
      dataTableToSqlAdapter.jdbcProperties()
          .putAll((Map<String, String>) planNode.getParams().get(JDBC_CONNECTION_PARAMS));
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
    return getDataTableFromResultSet(resultSet);
  }

  public static DataTable getDataTableFromResultSet(final ResultSet resultSet) throws SQLException {
    final List<String> columns = new ArrayList<>();
    final List<ColumnType> columnTypes = new ArrayList<>();
    final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
    final int columnCount = resultSetMetaData.getColumnCount();
    for (int i = 0; i < columnCount; i++) {
      columns.add(resultSetMetaData.getColumnLabel(i + 1));
      columnTypes.add(ColumnType.jdbcTypeToColumnType(resultSetMetaData.getColumnType(i + 1)));
    }
    final SimpleDataTableBuilder simpleDataTableBuilder = new SimpleDataTableBuilder(columns,
        columnTypes);
    while (resultSet.next()) {
      final Object[] rowData = simpleDataTableBuilder.newRow();
      for (int i = 0; i < columnCount; i++) {
        final ColumnType columnType = columnTypes.get(i);
        if (columnType.isArray()) {
          rowData[i] = resultSet.getArray(i + 1);
          continue;
        }
        switch (columnType.getType()) {
          case INT:
            rowData[i] = resultSet.getInt(i + 1);
            continue;
          case LONG:
            rowData[i] = resultSet.getLong(i + 1);
            continue;
          case DOUBLE:
            rowData[i] = resultSet.getDouble(i + 1);
            continue;
          case STRING:
            rowData[i] = resultSet.getString(i + 1);
            continue;
          case DATE:
            // todo cyril datetime is parsed as date - precision loss - use timestamp instead?
            rowData[i] = resultSet.getDate(i + 1);
            continue;
          case BOOLEAN:
            rowData[i] = resultSet.getBoolean(i + 1);
            continue;
          case BYTES:
            rowData[i] = resultSet.getBytes(i + 1);
            continue;
          default:
            throw new RuntimeException("Unrecognized data type - " + columnTypes.get(i + 1));
        }
      }
    }
    return simpleDataTableBuilder.build();
  }

  @Override
  public String getOperatorName() {
    return "SqlExecutionOperator";
  }
}
