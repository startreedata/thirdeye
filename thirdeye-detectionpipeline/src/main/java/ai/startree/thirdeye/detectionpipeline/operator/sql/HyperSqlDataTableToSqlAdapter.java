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
package ai.startree.thirdeye.detectionpipeline.operator.sql;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.Series.SeriesType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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

    final DataFrame df = dataTable.getDataFrame();
    // Create the table.
    createTable(c, tableName, df);

    // Insert all rows into the table
    for (int rowIdx = 0; rowIdx < df.size(); rowIdx++) {
      final String insertionStatement = getRowInsertionStatement(tableName, rowIdx, df);
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

  private void createTable(final Connection c, final String tableName, final DataFrame dataFrame)
      throws SQLException {
    final String tableCreationStatement = getTableCreationStatement(tableName,
        dataFrame.getSeriesNames(),
        dataFrame.getSeriesTypes());
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
      final DataFrame dataFrame) {
    final StringBuilder sb = new StringBuilder(
        "INSERT INTO " + tableName + " VALUES (");
    final List<String> seriesNames = dataFrame.getSeriesNames();
    for (int colIdx = 0; colIdx < seriesNames.size(); colIdx++) {
      final Object value = dataFrame.getObject(seriesNames.get(colIdx), rowIdx);

      // If string, then wrap with quotes
      final String quoteWith = value instanceof String ? "'" : "";
      sb
          .append(quoteWith)
          .append(value)
          .append(quoteWith);

      if (colIdx < seriesNames.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  private String getTableCreationStatement(final String tableName, final List<String> columns,
      final List<SeriesType> seriesTypes) {
    String tableCreationStatement = "CREATE TABLE " + tableName + " (";
    for (int i = 0; i < columns.size(); i++) {
      tableCreationStatement += columns.get(i) + " " + getColumnType(seriesTypes.get(i));
      if (i < columns.size() - 1) {
        tableCreationStatement += ", ";
      }
    }
    tableCreationStatement += ")";
    return tableCreationStatement;
  }

  private String getColumnType(final SeriesType seriesType) {
    switch (seriesType) {
      case LONG:
        return "BIGINT";
      case DOUBLE:
        return "DOUBLE";
      case STRING:
        return "VARCHAR(128)";
      case BOOLEAN:
        return "BOOLEAN";
      case OBJECT:
        return "VARBINARY(128)";
      default:
        throw new IllegalArgumentException("Unknown type " + seriesType.name());
    }
  }
}
