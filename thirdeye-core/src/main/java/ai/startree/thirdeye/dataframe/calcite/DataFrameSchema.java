package ai.startree.thirdeye.dataframe.calcite;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * Allow SQL on {@link DataFrame} with Calcite.
 *
 * The schema is mapped to dataframes. One table per dataframe.
 */
public class DataFrameSchema extends AbstractSchema {

  private final Map<String, DataFrame> dataframes;
  private Map<String, Table> tableMap;

  /**
   * Creates a DataFrame schema.
   *
   * @param dataframes map of dataframes
   */
  public DataFrameSchema(Map<String, DataFrame> dataframes) {
    super();
    this.dataframes = dataframes;
  }

  @Override
  protected Map<String, Table> getTableMap() {
    if (tableMap == null) {
      tableMap = createTableMap();
    }
    return tableMap;
  }

  private Map<String, Table> createTableMap() {
    // Build a map from table name to table - each dataframe becomes a table.
    final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
    for (Map.Entry<String, DataFrame> entry : dataframes.entrySet()) {
      if (entry.getValue() != null) {
        final Table table = createTable(entry.getValue());
        builder.put(entry.getKey(), table);
      }
    }
    return builder.build();
  }

  private Table createTable(DataFrame dataframe) {
    return new DataFrameFilterableTable(dataframe, null);
  }
}
