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
package ai.startree.thirdeye.datalayer.calcite.object;

import java.util.List;
import java.util.Map;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * Allow SQL on any Object with an ObjectToRelationAdapter.
 *
 * There is only one table in this schema, it corresponds to the list of objects.
 */
public class ObjectSchema<T> extends AbstractSchema {

  private static final String SINGLE_TABLE_NAME = "objects";

  private final List<T> elements;
  private final ObjectToRelationAdapter<T> adapter;
  private Map<String, Table> tableMap;

  public ObjectSchema(final List<T> elements, final ObjectToRelationAdapter<T> adapter) {
    super();
    this.elements = elements;
    this.adapter = adapter;
  }

  // return the original elements
  public List<T> getElements() {
    return elements;
  }

  public String singleTableName() {
    return SINGLE_TABLE_NAME;
  }

  @Override
  protected Map<String, Table> getTableMap() {
    if (tableMap == null) {
      final Table table = new ObjectTable<>(elements, adapter);
      tableMap = Map.of(SINGLE_TABLE_NAME, table);
    }
    return tableMap;
  }
}
