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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Table based on a DataFrame. Implements simple equal filtering.
 *
 * <p>It implements the {@link FilterableTable} interface, so Calcite gets
 * data by calling the {@link #scan(DataContext, List)} method.
 *
 * Resource: https://github.com/apache/calcite/blob/4bc916619fd286b2c0cc4d5c653c96a68801d74e/example/csv/src/main/java/org/apache/calcite/adapter/csv/CsvFilterableTable.java
 */
public class DataFrameFilterableTable extends DataFrameTable
    implements FilterableTable {

  public DataFrameFilterableTable(DataFrame dataFrame, RelProtoDataType protoRowType) {
    super(dataFrame, protoRowType);
  }

  @Override
  public String toString() {
    return "DataFrameFilterableTable";
  }

  @Override
  public Enumerable<@Nullable Object[]> scan(DataContext root, List<RexNode> filters) {
    JavaTypeFactory typeFactory = root.getTypeFactory();
    final List<DataFrameFieldType> fieldTypes = getFieldTypes(typeFactory);
    final @Nullable Object[] filterValues = new Object[fieldTypes.size()];
    filters.removeIf(filter -> addFilter(filter, filterValues));
    final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
    return new AbstractEnumerable<@Nullable Object[]>() {
      @Override
      public Enumerator<@Nullable Object[]> enumerator() {
        return new DataFrameEnumerator(dataFrame, cancelFlag, filterValues);
      }
    };
  }

  /**
   * Push down equal filter for STRING, LONG and DOUBLE.
   */
  private static boolean addFilter(RexNode filter, @Nullable Object[] filterValues) {

    if (filter.isA(SqlKind.AND)) {
      // We cannot refine(remove) the operands of AND,
      // it will cause o.a.c.i.TableScanNode.createFilterable filters check failed.
      ((RexCall) filter).getOperands().forEach(subFilter -> addFilter(subFilter, filterValues));
    } else if (filter.isA(SqlKind.EQUALS)) {
      final RexCall call = (RexCall) filter;
      RexNode left = call.getOperands().get(0);
      if (left.isA(SqlKind.CAST)) {
        left = ((RexCall) left).operands.get(0);
      }
      final RexNode right = call.getOperands().get(1);
      if (left instanceof RexInputRef
          && right instanceof RexLiteral) {
        final int index = ((RexInputRef) left).getIndex();
        if (filterValues[index] == null) {
          filterValues[index] = customGetValue((RexLiteral) right);
          return true;
        }
      }
    }
    return false;
  }

  private static Object customGetValue(RexLiteral literal) {
    // only tested with SeriesType DOUBLE, BOOLEAN, STRING, LONG
    SqlTypeName preciseSqlType = literal.getType().getSqlTypeName();
    switch (preciseSqlType) {
      case DOUBLE:
        return literal.getValueAs(Double.class);
      case BOOLEAN:
        return literal.getValueAs(Boolean.class);
      default:
        // sensible defaults: STRING and LONG managed correctly
        return literal.getValue2();
    }
  }
}
