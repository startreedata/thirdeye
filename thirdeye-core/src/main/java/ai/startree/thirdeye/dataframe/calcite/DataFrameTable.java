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
package ai.startree.thirdeye.dataframe.calcite;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.Series;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Table for SQL on a TE DataFrame.
 * Resource: https://github.com/apache/calcite/blob/4bc916619fd286b2c0cc4d5c653c96a68801d74e/example/csv/src/main/java/org/apache/calcite/adapter/csv/CsvTable.java
 */
public abstract class DataFrameTable extends AbstractTable {

  protected final DataFrame dataFrame;
  protected final @Nullable RelProtoDataType protoRowType;
  private @Nullable RelDataType rowType;
  private @Nullable List<DataFrameFieldType> fieldTypes;

  /**
   * Creates a DataFrameTable.
   */
  DataFrameTable(DataFrame dataFrame, @Nullable RelProtoDataType protoRowType) {
    this.dataFrame = dataFrame;
    this.protoRowType = protoRowType;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    if (protoRowType != null) {
      return protoRowType.apply(typeFactory);
    }
    if (rowType == null) {
      rowType = deduceTypes((JavaTypeFactory) typeFactory, dataFrame);
    }
    return rowType;
  }

  private RelDataType deduceTypes(final JavaTypeFactory typeFactory, final DataFrame dataFrame) {
    return deduceRowType(typeFactory, dataFrame, fieldTypes);
  }

  /**
   * Returns the field types of the DataFrame table.
   */
  public List<DataFrameFieldType> getFieldTypes(RelDataTypeFactory typeFactory) {
    if (fieldTypes == null) {
      fieldTypes = new ArrayList<>();
      deduceTypes((JavaTypeFactory) typeFactory, dataFrame);
    }
    return fieldTypes;
  }

  /**
   * Deduces the names and types of a table's columns
   */
  private static RelDataType deduceRowType(JavaTypeFactory typeFactory,
      DataFrame dataFrame, @Nullable List<DataFrameFieldType> fieldTypes) {
    final List<RelDataType> types = new ArrayList<>();
    final List<String> names = new ArrayList<>();
    for (Map.Entry<String, Series> entry : dataFrame.getSeries().entrySet()) {
      final DataFrameFieldType fieldType = DataFrameFieldType.of(entry.getValue().type());
      names.add(entry.getKey());
      types.add(fieldType.toType(typeFactory));
      if (fieldTypes != null) {
        fieldTypes.add(fieldType);
      }
    }
    return typeFactory.createStructType(Pair.zip(names, types));
  }
}
