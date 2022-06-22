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

import ai.startree.thirdeye.dataframe.calcite.DataFrameSchema;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import com.google.common.collect.ImmutableMap;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

/**
 * Perform SQL on a DataFrame with Calcite.
 * Functions corresponding to specific SQL dialect can be added: "bigquery", "mysql", "oracle",
 * "postgresql" and "spatial"
 * See https://calcite.apache.org/docs/reference.html#dialect-specific-operators
 * By default, add bigquery functions: to have TIMESTAMP_MILLIS(integer) and UNIX_MILLIS(timestamp)
 * available.
 * Window and analytical functions are supported.
 */
public class CalciteDataTableToSqlAdapter implements DataTableToSqlAdapter {

  private static final String DATAFRAME_DATABASE = "calcite_dataframes";
  private static final Properties DEFAULT_JDBC_PROPERTIES = new Properties();

  static {
    DEFAULT_JDBC_PROPERTIES.putAll(ImmutableMap.of(
        "unquotedCasing", "UNCHANGED",
        "fun", "bigquery"));
  }

  private final Properties properties = new Properties(DEFAULT_JDBC_PROPERTIES);

  @Override
  public String jdbcConnection() {
    return "jdbc:calcite:";
  }

  @Override
  public String jdbcDriverClassName() {
    return "org.apache.calcite.jdbc.Driver";
  }

  @Override
  public Properties jdbcProperties() {
    return properties;
  }

  @Override
  public void loadTables(final Connection connection, final Map<String, DataTable> dataTables)
      throws SQLException {
    Map<String, DataFrame> dataframes = dataTables.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDataFrame()));
    DataFrameSchema schema = new DataFrameSchema(dataframes);
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    rootSchema.add(DATAFRAME_DATABASE, schema);
    // equivalent to "use [DATAFRAME_DATABASE]";
    calciteConnection.setSchema(DATAFRAME_DATABASE);
  }

  @Override
  public void tearDown(final Connection connection) {
    // nothing to do - GC should be enough
  }
}
