package org.apache.pinot.thirdeye.detection.v2.operator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable.SimpleDataTableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connecting to a temporary SQL workspace with all the inputs data as tables and perform SQL
 * operations on top.
 */
public class SqlExecutionOperator extends DetectionPipelineOperator {

  public static final Logger LOGGER = LoggerFactory.getLogger(SqlExecutionOperator.class);
  private static final String JDBC_DRIVER_CLASSNAME = "jdbc.driver.classname";
  private static final String JDBC_CONNECTION = "jdbc.connection";
  private static final String SQL_QUERIES = "sql.queries";
  private String jdbcDriverClassName = "org.hsqldb.jdbc.JDBCDriver";
  private String jdbcConnection = "jdbc:hsqldb:mem";
  private List<String> queries;

  public SqlExecutionOperator() {
    super();
  }


  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    for (OutputBean outputBean : context.getPlanNode().getOutputs()) {
      outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
    }
    if (planNode.getParams().containsKey(SQL_QUERIES)) {
      queries = (List<String>) planNode.getParams().get(SQL_QUERIES);
    } else {
      throw new IllegalArgumentException(
          "Missing property '" + SQL_QUERIES + "' in SqlExecutionOperator");
    }
    if (planNode.getParams().containsKey(JDBC_DRIVER_CLASSNAME)) {
      jdbcDriverClassName = planNode.getParams().get(JDBC_DRIVER_CLASSNAME).toString();
    }
    if (planNode.getParams().containsKey(JDBC_CONNECTION)) {
      jdbcConnection = planNode.getParams().get(JDBC_CONNECTION).toString();
    }
  }

  @Override
  protected BaseComponent createComponent() {
    return null;
  }

  @Override
  public void execute() throws Exception {
    try {
      Class.forName(jdbcDriverClassName);
    } catch (Exception e) {
      LOGGER.error("ERROR: failed to load JDBC driver class {}.", jdbcDriverClassName, e);
      throw e;
    }
    Connection c = DriverManager.getConnection(jdbcConnection);
    LOGGER.debug("Successfully connected to JDBC connection: {} with driver class: {} ",
        jdbcConnection,
        jdbcDriverClassName);

    List<String> insertedTable = new ArrayList<>();
    for (String tableName : inputMap.keySet()) {
      DetectionPipelineResult detectionPipelineResult = inputMap.get(tableName);
      if (detectionPipelineResult instanceof DataTable) {
        insertInput(c, tableName, (DataTable) detectionPipelineResult);
        insertedTable.add(tableName);
      }
    }
    int i = 0;
    for (String query : queries) {
      try {
        final Statement stmt = c.createStatement();
        final ResultSet resultSet = stmt.executeQuery(query);
        final DataTable dataTableFromResultSet = getDataTableFromResultSet(resultSet);
        setOutput(Integer.toString(i++), dataTableFromResultSet);
      } catch (SQLException e) {
        LOGGER.error("Got exceptions when executing SQL query: {}", query, e);
        throw e;
      }
    }

    // Destroy database
    LOGGER.debug("trying to drop all the tables to clean up the environment.");
    for (String tableName : insertedTable) {
      destroyTable(c, tableName);
    }
  }

  private DataTable getDataTableFromResultSet(final ResultSet resultSet) throws SQLException {
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
      Object[] rowData = simpleDataTableBuilder.newRow();
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

  private void destroyTable(final Connection c, final String tableName) throws SQLException {
    String dropTableStatement = "DROP TABLE " + tableName + " IF EXISTS";
    try {
      c.prepareCall(dropTableStatement).execute();
    } catch (SQLException e) {
      LOGGER.error("Failed to drop table: {} with sql: {}",
          tableName,
          dropTableStatement,
          e);
      throw e;
    }
  }

  private void insertInput(final Connection c, final String tableName,
      final DataTable dataTable) throws SQLException {
    // Drop the table in case.
    destroyTable(c, tableName);
    // Create the table.
    final String tableCreationStatement = getTableCreationStatement(tableName,
        dataTable.getColumns(),
        dataTable.getColumnTypes());
    try {
      c.prepareCall(tableCreationStatement).execute();
      LOGGER.debug("Trying to create table with sql: {}", tableCreationStatement);
    } catch (SQLException e) {
      LOGGER.error("Failed to create table: {} with sql: {}",
          tableName,
          tableCreationStatement,
          e);
      throw e;
    }

    // Insert all rows into the table
    for (int rowIdx = 0; rowIdx < dataTable.getRowCount(); rowIdx++) {
      final String insertionStatement = getRowInsertionStatement(tableName, rowIdx, dataTable);
      try {
        c.prepareCall(insertionStatement).execute();
      } catch (SQLException e) {
        LOGGER.error("Failed to insert row idx: {}, insertion sql: {}",
            rowIdx,
            insertionStatement,
            e);
        throw e;
      }
    }
  }

  private String getRowInsertionStatement(final String tableName, final int rowIdx,
      final DataTable dataTable) {
    String insertionStatement = "INSERT INTO " + tableName + " VALUES (";
    for (int colIdx = 0; colIdx < dataTable.getColumnCount(); colIdx++) {
      insertionStatement += dataTable.getObject(rowIdx, colIdx);
      if (colIdx < dataTable.getColumnCount() - 1) {
        insertionStatement += ", ";
      }
    }
    insertionStatement += ")";
    return insertionStatement;
  }

  private String getTableCreationStatement(final String tableName, final List<String> columns,
      final List<ColumnType> columnTypes) {
    String tableCreationStatement = "CREATE TABLE " + tableName + " (";
    for (int i = 0; i < columns.size(); i++) {
      tableCreationStatement += columns.get(i) + " " + getColumnType(columnTypes.get(i));
      if (i < columns.size() - 1) {
        tableCreationStatement += ", ";
      }
    }
    tableCreationStatement += ")";
    return tableCreationStatement;
  }

  private String getColumnType(final ColumnType columnType) {
    switch (columnType.getType()) {
      case INT:
      case LONG:
        return "BIGINT";
      case FLOAT:
      case DOUBLE:
        return "DOUBLE";
      case STRING:
        return "VARCHAR(128)";
      case BYTES:
      case OBJECT:
        return "VARBINARY(128)";
    }
    return columnType.getType().toString();
  }

  @Override
  public String getOperatorName() {
    return "SqlExecutionOperator";
  }
}
