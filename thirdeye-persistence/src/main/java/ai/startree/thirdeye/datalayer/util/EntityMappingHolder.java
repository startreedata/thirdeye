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

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityMappingHolder {

  private static final Logger LOG = LoggerFactory.getLogger(EntityMappingHolder.class);
  private static final int COLUMN_NAME_INDEX = 4;

  //Map<TableName,EntityName>
  final BiMap<String, String> tableToEntityNameMap = HashBiMap.create();
  final Map<String, LinkedHashMap<String, ColumnInfo>> columnInfoPerTable = new HashMap<>();
  //DB NAME to ENTITY NAME mapping
  final Map<String, BiMap<String, String>> columnMappingPerTable = new HashMap<>();

  private static List<Field> getAllFields(List<Field> fields, final Class<?> type) {
    fields.addAll(Arrays.asList(type.getDeclaredFields()));
    if (type.getSuperclass() != null) {
      fields = getAllFields(fields, type.getSuperclass());
    }
    return fields;
  }

  private static ResultSet getColumns(final DatabaseMetaData databaseMetaData,
      final String tableNamePattern)
      throws SQLException {
    return databaseMetaData.getColumns(null, null, tableNamePattern, null);
  }

  private static LinkedHashMap<String, ColumnInfo> buildColumnInfoMap(final String tableName,
      final DatabaseMetaData databaseMetaData) throws SQLException {
    final LinkedHashMap<String, ColumnInfo> columnInfoMap = new LinkedHashMap<>();
    final var tableNamePatterns = List.of(tableName.toLowerCase(), tableName.toUpperCase());

    for (final String tableNamePattern : tableNamePatterns) {
      try (final ResultSet rs = getColumns(databaseMetaData, tableNamePattern)) {
        while (rs.next()) {
          final String columnName = rs.getString(COLUMN_NAME_INDEX).toLowerCase();
          final ColumnInfo columnInfo = new ColumnInfo()
              .setColumnNameInDB(columnName)
              .setSqlType(rs.getInt(5));

          columnInfoMap.put(columnName, columnInfo);
        }
      }
    }
    return columnInfoMap;
  }

  private static List<Field> getFields(final Class<? extends AbstractEntity> entityClass) {
    final List<Field> fields = new ArrayList<>();
    getAllFields(fields, entityClass);
    return fields;
  }

  public void register(final Connection connection,
      final Class<? extends AbstractEntity> entityClass,
      String tableName) throws Exception {
    tableName = tableName.toLowerCase();
    final DatabaseMetaData databaseMetaData = connection.getMetaData();
    tableToEntityNameMap.put(tableName, entityClass.getSimpleName());
    columnMappingPerTable.put(tableName, HashBiMap.create());

    final var columnInfoMap = buildColumnInfoMap(tableName, databaseMetaData);
    checkState(!columnInfoMap.isEmpty(), "Unable to find table: " + tableName);

    populateColumnInfoMap(entityClass, tableName, columnInfoMap);
    columnInfoPerTable.put(tableName, columnInfoMap);
  }

  private void populateColumnInfoMap(final Class<? extends AbstractEntity> entityClass,
      final String tableName,
      final LinkedHashMap<String, ColumnInfo> columnInfoMap) {
    final List<Field> fields = getFields(entityClass);

    for (final String dbColumn : columnInfoMap.keySet()) {
      boolean success = false;
      for (final Field field : fields) {
        field.setAccessible(true);
        final String entityColumn = field.getName();
        if (dbColumn.equalsIgnoreCase(entityColumn)) {
          success = true;
        }
        final String dbColumnNormalized = dbColumn.replaceAll("_", "").toLowerCase();
        final String entityColumnNormalized = entityColumn.replaceAll("_", "").toLowerCase();
        if (dbColumnNormalized.equals(entityColumnNormalized)) {
          success = true;
        }
        if (success) {
          columnInfoMap.get(dbColumn).setColumnNameInEntity(entityColumn);
          columnInfoMap.get(dbColumn).setField(field);
          columnMappingPerTable.get(tableName).put(dbColumn, entityColumn);
          break;
        }
      }
      if (!success) {
        LOG.error(String.format("Unable to map [%s] to any field in table [%s] !!!",
            dbColumn,
            entityClass.getSimpleName()));
      }
    }
  }
}


