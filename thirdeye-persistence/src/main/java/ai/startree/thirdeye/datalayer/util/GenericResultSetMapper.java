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
package ai.startree.thirdeye.datalayer.util;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Singleton
public class GenericResultSetMapper {

  private final EntityMappingHolder entityMappingHolder;

  @Inject
  public GenericResultSetMapper(final EntityMappingHolder entityMappingHolder) {
    this.entityMappingHolder = entityMappingHolder;
  }

  public <E extends AbstractEntity> E mapSingle(final ResultSet rs,
      final Class<? extends AbstractEntity> entityClass)
      throws Exception {
    final List<E> resultMapList = (List<E>) toEntityList(rs, entityClass);
    if (resultMapList.size() > 0) {
      return resultMapList.get(0);
    }
    return null;
  }

  public <E extends AbstractEntity> List<E> mapAll(final ResultSet rs,
      final Class<E> entityClass) throws Exception {
    return toEntityList(rs, entityClass);
  }

  private <E extends AbstractEntity> List<E> toEntityList(final ResultSet rs,
      final Class<E> entityClass) throws Exception {
    final String tableName =
        entityMappingHolder.tableToEntityNameMap.inverse().get(entityClass.getSimpleName());
    final LinkedHashMap<String, ColumnInfo> columnInfoMap =
        entityMappingHolder.columnInfoPerTable.get(tableName);
    final List<E> entityList = new ArrayList<>();

    final ObjectMapper mapper = new ObjectMapper();
    while (rs.next()) {
      final AbstractEntity entityObj;
      final ResultSetMetaData resultSetMetaData = rs.getMetaData();
      final int numColumns = resultSetMetaData.getColumnCount();
      final ObjectNode objectNode = mapper.createObjectNode();
      for (int i = 1; i <= numColumns; i++) {
        final String dbColumnName = resultSetMetaData.getColumnLabel(i).toLowerCase();
        final ColumnInfo columnInfo = columnInfoMap.get(dbColumnName);
        final Field field = columnInfo.getField();
        final Object val;
        if (columnInfo.getSqlType() == Types.CLOB) {
          final Clob clob = rs.getClob(i);
          val = clob.getSubString(1, (int) clob.length());
        } else {
          val = rs.getObject(i);
        }
        if (val == null) {
          continue;
        }
        if (field.getType().isAssignableFrom(Timestamp.class)) {
          objectNode.put(field.getName(), ((Timestamp) val).getTime());
        } else {
          objectNode.put(field.getName(), val.toString());
        }
      }
      entityObj = mapper.treeToValue(objectNode, entityClass);
      entityList.add((E) entityObj);
    }

    return entityList;
  }
}
