/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.detection.v2;

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
