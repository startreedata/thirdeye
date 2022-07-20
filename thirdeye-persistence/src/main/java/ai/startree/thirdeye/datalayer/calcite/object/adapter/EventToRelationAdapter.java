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
package ai.startree.thirdeye.datalayer.calcite.object.adapter;

import ai.startree.thirdeye.datalayer.calcite.filter.ObjectWithIdToRelationAdapter;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.commons.collections4.map.DefaultedMap;

public class EventToRelationAdapter implements ObjectWithIdToRelationAdapter<EventDTO> {

  private static final String ID_COLUMN = "_id";

  @Override
  public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
    return typeFactory.builder()
        .add(ID_COLUMN, typeFactory.createSqlType(SqlTypeName.BIGINT))
        .add("type", typeFactory.createSqlType(SqlTypeName.VARCHAR))
        .add("dimensionMap",
            typeFactory.createMapType(
                typeFactory.createSqlType(SqlTypeName.VARCHAR),
                typeFactory.createMultisetType(typeFactory.createSqlType(SqlTypeName.VARCHAR), 1000)
            )
        )
        .build();
  }

  @Override
  public Object[] getRow(final EventDTO element) {
    // a default map make the sql easier to write for dimension values - because MEMBER OF fails if a collection is null
    final DefaultedMap<String, List<String>> defaultMap = new DefaultedMap<>(List.of());
    defaultMap.putAll(element.getTargetDimensionMap());

    return new Object[]{element.getId(), element.getEventType(), defaultMap};
  }

  @Override
  public Long idOf(final EventDTO element) {
    return element.getId();
  }

  @Override
  public String idColumn() {
    return ID_COLUMN;
  }
}
