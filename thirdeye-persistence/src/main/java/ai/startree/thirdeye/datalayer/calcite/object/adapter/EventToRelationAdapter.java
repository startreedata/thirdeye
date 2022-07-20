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
