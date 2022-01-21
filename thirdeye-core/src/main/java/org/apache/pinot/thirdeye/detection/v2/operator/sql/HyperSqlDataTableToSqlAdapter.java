package org.apache.pinot.thirdeye.detection.v2.operator.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyperSqlDataTableToSqlAdapter implements DataTableToSqlAdapter {

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private final List<String> insertedTable = new ArrayList<>();

  private final Properties properties = new Properties();

  @Override
  public String jdbcConnection() {
    return "jdbc:hsqldb:mem";
  }

  @Override
  public String jdbcDriverClassName() {
    return "org.hsqldb.jdbc.JDBCDriver";
  }

  @Override
  public Properties jdbcProperties() {
    return properties;
  }

  @Override
  public void loadTables(final Connection connection, final Map<String, DataTable> dataTables)
      throws SQLException {
    for (final Entry<String, DataTable> entry : dataTables.entrySet()) {
      insertInput(connection, entry.getKey(), entry.getValue());
      insertedTable.add(entry.getKey());
    }
  }

  @Override
  public void tearDown(final Connection connection) throws SQLException {
    // Destroy database
    LOG.debug("trying to drop all the tables to clean up the environment.");
    for (final String tableName : insertedTable) {
      destroyTable(connection, tableName);
    }
  }

  private void destroyTable(final Connection c, final String tableName) throws SQLException {
    final String dropTableStatement = "DROP TABLE " + tableName + " IF EXISTS";
    try {
      c.prepareCall(dropTableStatement).execute();
    } catch (final SQLException e) {
      LOG.error("Failed to drop table: {} with sql: {}",
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
    createTable(c, tableName, dataTable);

    // Insert all rows into the table
    for (int rowIdx = 0; rowIdx < dataTable.getRowCount(); rowIdx++) {
      final String insertionStatement = getRowInsertionStatement(tableName, rowIdx, dataTable);
      try {
        c.prepareCall(insertionStatement).execute();
      } catch (final SQLException e) {
        LOG.error("Failed to insert row idx: {}, insertion sql: {}",
            rowIdx,
            insertionStatement,
            e);
        throw e;
      }
    }
  }

  private void createTable(final Connection c, final String tableName, final DataTable dataTable)
      throws SQLException {
    final String tableCreationStatement = getTableCreationStatement(tableName,
        dataTable.getColumns(),
        dataTable.getColumnTypes());
    try {
      c.prepareCall(tableCreationStatement).execute();
      LOG.debug("Trying to create table with sql: {}", tableCreationStatement);
    } catch (final SQLException e) {
      LOG.error("Failed to create table: {} with sql: {}",
          tableName,
          tableCreationStatement,
          e);
      throw e;
    }
  }

  private String getRowInsertionStatement(final String tableName,
      final int rowIdx,
      final DataTable dataTable) {
    final StringBuilder sb = new StringBuilder(
        "INSERT INTO " + tableName + " VALUES (");
    for (int colIdx = 0; colIdx < dataTable.getColumnCount(); colIdx++) {
      final Object value = dataTable.getObject(rowIdx, colIdx);

      // If string, then wrap with quotes
      final String quoteWith = value instanceof String ? "'" : "";
      sb
          .append(quoteWith)
          .append(value)
          .append(quoteWith);

      if (colIdx < dataTable.getColumnCount() - 1) {
        sb.append(", ");
      }
    }
    sb.append(")");
    return sb.toString();
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
}
