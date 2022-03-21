package org.apache.pinot.thirdeye.spi.detection.v2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * An adapter to run SQL on a DataTable with jdbc.
 * Provides required jdbc info and methods to create SQL tables from DataTables.
 * Does not manage connection and query execution.
 */
public interface DataTableToSqlAdapter {

  String jdbcConnection();

  String jdbcDriverClassName();

  /**
   * Returns properties of the jdbc connection.
   * The Properties can be updated.
   */
  Properties jdbcProperties();

  void loadTables(final Connection connection, Map<String, DataTable> dataTables)
      throws SQLException;

  void tearDown(final Connection connection) throws SQLException;
}
