package org.apache.pinot.thirdeye.detection.v2.operator.sql;

import java.util.Locale;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTableToSqlAdapter;

public class DataTableToSqlAdapterFactory {

  public static DataTableToSqlAdapter create(final String sqlEngine) {
    switch (sqlEngine.toUpperCase(Locale.ENGLISH)) {
      case "CALCITE":
        return new CalciteDataTableToSqlAdapter();
      case "HYPERSQL": case "HSQLDB":
        return new HyperSqlDataTableToSqlAdapter();
      default:
        throw new IllegalArgumentException(String.format("Unknown SQL engine: %s", sqlEngine));
    }
  }
}
