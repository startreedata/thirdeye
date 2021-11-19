package org.apache.pinot.thirdeye.detection.v2.operator;

import com.google.common.collect.ImmutableMap;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.pinot.thirdeye.dataframe.calcite.DataFrameSchema;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;

/**
 * Perform SQL on a DataFrame with Calcite.
 * Functions corresponding to specific SQL dialect can be added: "bigquery", "mysql", "oracle",
 * "postgresql" and "spatial"
 * See https://calcite.apache.org/docs/reference.html#dialect-specific-operators
 * By default, add bigquery functions: to have TIMESTAMP_MILLIS(integer) and UNIX_MILLIS(timestamp)
 * available.
 * Window and analytical functions are supported.
 */
public class CalciteSqlExecutionOperator extends AbstractSqlExecutionOperator {

  private static final String DATAFRAME_DATABASE = "calcite_dataframes";

  public CalciteSqlExecutionOperator() {
    super();
  }

  @Override
  protected String getDefaultJdbcConnection() {
    return "jdbc:calcite:";
  }

  @Override
  protected String getDefaultJdbcDriverClassName() {
    return "org.apache.calcite.jdbc.Driver";
  }

  @Override
  protected Map<String, String> getDefaultJdbcProperties() {

    return ImmutableMap.of(
        "unquotedCasing", "UNCHANGED",
        "fun", "bigquery"
    );
  }

  @Override
  protected void initTables(final Connection connection) throws SQLException {
    CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
    Map<String, DataFrame> dataframes = new HashMap<>();
    for (String tableName : inputMap.keySet()) {
      DetectionPipelineResult detectionPipelineResult = inputMap.get(tableName);
      if (detectionPipelineResult instanceof DataTable) {
        DataFrame tableDf = ((DataTable) detectionPipelineResult).getDataFrame();
        dataframes.put(tableName, tableDf);
      }
    }
    DataFrameSchema schema = new DataFrameSchema(dataframes);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    rootSchema.add(DATAFRAME_DATABASE, schema);
    // equivalent to "use [DATAFRAME_DATABASE]";
    calciteConnection.setSchema(DATAFRAME_DATABASE);
  }

  @Override
  protected void tearDown(final Connection connection) {
    // nothing to do - GC should be enough
  }

  @Override
  public String getOperatorName() {
    return "CalciteSqlExecutionOperator";
  }
}
