/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.operator.sql;

import ai.startree.thirdeye.spi.detection.v2.DataTableToSqlAdapter;
import java.util.Locale;

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
