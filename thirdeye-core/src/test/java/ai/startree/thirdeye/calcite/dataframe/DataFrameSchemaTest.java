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
package ai.startree.thirdeye.calcite.dataframe;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataFrameSchemaTest {

  private static final String CALCITE_JDBC_URL = "jdbc:calcite:";
  private static final String ROOT_SCHEMA = "root_schema";

  private Properties properties;
  private DataFrame dataFrame;

  @BeforeMethod
  public void init() {
    dataFrame = new DataFrame();
    dataFrame.addSeries("str_column", "PLACED", "IN_PROGRESS", "PLACED", "IN_PROGRESS");
    dataFrame.addSeries("boolean_column", true, false, true, false);
    dataFrame.addSeries("long_column",
        1567631719000L,
        1568549798000L,
        1568549798000L,
        1570095890000L);
    dataFrame.addSeries("double_column", 1.1D, 1.2D, 1.3D, 1.4D);

    // if not set, casing is changed when query is sent
    properties = new Properties();
    properties.setProperty("unquotedCasing", "UNCHANGED");
  }

  @Test
  public void testPredicatePushdown() throws Exception {
    // test String/Long/Double equal predicate push down to DataFrameEnumerator
    final Map<String, DataFrame> dataframes = ImmutableMap.of(
        "types_table", dataFrame
    );
    DataFrameSchema schema = new DataFrameSchema(dataframes);
    Connection connection = getConnection(schema, properties);
    Statement statement = connection.createStatement();

    ResultSet resultSet = statement.executeQuery(
        "select str_column, boolean_column, long_column, double_column "
            + "from types_table "
            + "where str_column='PLACED' "
            + "AND long_column=1567631719000 "
            + "AND double_column=1.1 ");
    final DataTable dataTableFromResultSet = SimpleDataTable.fromDataFrame(
        DataFrame.fromResultSet(resultSet));
    final DataFrame outputDf = dataTableFromResultSet.getDataFrame();

    final DataFrame expectedDf = new DataFrame();
    expectedDf.addSeries("str_column", "PLACED");
    expectedDf.addSeries("boolean_column", true);
    expectedDf.addSeries("long_column", 1567631719000L);
    expectedDf.addSeries("double_column", 1.1D);

    Assert.assertEquals(outputDf, expectedDf);
  }

  @Test
  public void testMultipleTables() throws Exception {
    // test importing multiple dataframe as SQL tables
    final DataFrame ownerDataFrame = new DataFrame();
    ownerDataFrame.addSeries("status", "PLACED", "IN_PROGRESS", "SHIPPED");
    ownerDataFrame.addSeries("owner", "John", "Billy", "James");

    final Map<String, DataFrame> dataframes = ImmutableMap.of(
        "types_table", dataFrame,
        "owner_table", ownerDataFrame
    );
    final DataFrameSchema schema = new DataFrameSchema(dataframes);
    final Connection connection = getConnection(schema, properties);
    final Statement statement = connection.createStatement();
    statement.executeQuery(
        "select owner, str_column, boolean_column, long_column, double_column from types_table join owner_table on str_column = status");

    // query success is enough
    assert true;
  }

  @Test
  public void testCustomDialectFunctions() throws Exception {
    // test using custom SQL dialect BigQuery
    final Map<String, DataFrame> dataframes = ImmutableMap.of(
        "types_table", dataFrame
    );
    final DataFrameSchema schema = new DataFrameSchema(dataframes);
    // add custom dialect bigquery functions
    properties.setProperty("fun", "bigquery");
    final Connection connection = getConnection(schema, properties);
    final Statement statement = connection.createStatement();
    statement.executeQuery(
        "select TIMESTAMP_MILLIS(long_column), UNIX_MILLIS(TIMESTAMP_MILLIS(long_column)) from types_table");

    // query success is enough
    assert true;
  }

  private Connection getConnection(final DataFrameSchema schema, final Properties properties)
      throws SQLException {
    final Connection connection = DriverManager.getConnection(CALCITE_JDBC_URL, properties);
    final CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    final SchemaPlus rootSchema = calciteConnection.getRootSchema();
    rootSchema.add(ROOT_SCHEMA, schema);
    calciteConnection.setSchema(ROOT_SCHEMA);

    return calciteConnection;
  }
}
