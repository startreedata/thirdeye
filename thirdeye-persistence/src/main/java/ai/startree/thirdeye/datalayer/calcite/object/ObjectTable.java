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
package ai.startree.thirdeye.datalayer.calcite.object;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Table from a list of objects. The conversion from object to columns is given by the
 * ObjectToRelationAdapter.
 */
public class ObjectTable<T> extends AbstractTable implements ScannableTable {

  private final @NonNull List<T> elements;
  private final @NonNull ObjectToRelationAdapter<T> adapter;
  private @Nullable RelDataType rowType;

  public ObjectTable(final @NonNull List<T> elements, final @NonNull ObjectToRelationAdapter<T> adapter) {
    this.elements = elements;
    this.adapter = adapter;
  }

  @Override
  public Enumerable<Object[]> scan(final DataContext root) {
    final AtomicBoolean cancelFlag = DataContext.Variable.CANCEL_FLAG.get(root);
    return new AbstractEnumerable<>() {
      @Override
      public Enumerator<@Nullable Object[]> enumerator() {
        return new ObjectEnumerator<>(elements, cancelFlag, adapter);
      }
    };
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    if (rowType == null) {
      rowType = adapter.getRowType(typeFactory);
    }
    return rowType;
  }
}
