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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Array;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SqlQueryBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(SqlQueryBuilder.class);

  private static final String NAME_REGEX = "[a-z][_a-z0-9]*";
  private static final String PARAM_REGEX = ":(" + NAME_REGEX + ")";
  private static final Pattern PARAM_PATTERN =
      Pattern.compile(PARAM_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Set<String> AUTO_UPDATE_COLUMN_SET =
      Sets.newHashSet("id", "last_modified");
  //insert sql per table
  private final Map<String, String> insertSqlMap = new HashMap<>();
  private final EntityMappingHolder entityMappingHolder;

  @Inject
  public SqlQueryBuilder(final EntityMappingHolder entityMappingHolder) {
    this.entityMappingHolder = entityMappingHolder;
  }

  public static String generateInsertSql(final String tableName,
      final LinkedHashMap<String, ColumnInfo> columnInfoMap) {

    final StringBuilder values = new StringBuilder(" VALUES");
    final StringBuilder names = new StringBuilder();
    names.append("(");
    values.append("(");
    String delim = "";
    for (final ColumnInfo columnInfo : columnInfoMap.values()) {
      final String columnName = columnInfo.getColumnNameInDB();
      if (columnInfo.getField() != null && !AUTO_UPDATE_COLUMN_SET.contains(columnName.toLowerCase())) {
        names.append(delim);
        names.append(columnName);
        values.append(delim);
        values.append("?");
        delim = ",";
      }
    }
    names.append(")");
    values.append(")");

    return "INSERT INTO " + tableName + names + values;
  }

  public PreparedStatement createInsertStatement(final Connection conn, final AbstractEntity entity)
      throws Exception {
    final String tableName = requireNonNull(
        entityMappingHolder.tableToEntityNameMap.inverse().get(entity.getClass().getSimpleName()));
    return createInsertStatement(conn, tableName, entity);
  }

  public PreparedStatement createInsertStatement(final Connection conn, final String tableName,
      final AbstractEntity entity) throws Exception {
    if (!insertSqlMap.containsKey(tableName)) {
      final String insertSql = generateInsertSql(tableName,
          entityMappingHolder.columnInfoPerTable.get(tableName.toLowerCase()));
      insertSqlMap.put(tableName, insertSql);
    }

    final String sql = insertSqlMap.get(tableName);
    final PreparedStatement preparedStatement =
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    final LinkedHashMap<String, ColumnInfo> columnInfoMap =
        entityMappingHolder.columnInfoPerTable.get(tableName);
    int parameterIndex = 1;
    for (final ColumnInfo columnInfo : columnInfoMap.values()) {
      if (columnInfo.getField() != null
          && !AUTO_UPDATE_COLUMN_SET.contains(columnInfo.getColumnNameInDB().toLowerCase())) {
        final Object val = columnInfo.getField().get(entity);
        if (val != null) {
          if (columnInfo.getSqlType() == Types.CLOB) {
            final Clob clob = conn.createClob();
            clob.setString(1, val.toString());
            preparedStatement.setClob(parameterIndex++, clob);
          } else if (columnInfo.getSqlType() == Types.TIMESTAMP) {
            preparedStatement.setObject(parameterIndex++, val, columnInfo.getSqlType());
          } else {
            preparedStatement.setObject(parameterIndex++, val.toString(), columnInfo.getSqlType());
          }
        } else {
          preparedStatement.setNull(parameterIndex++, columnInfo.getSqlType());
        }
      }
    }
    return preparedStatement;
  }

  public PreparedStatement createUpdateStatement(final Connection connection, final AbstractEntity entity,
      final Set<String> fieldsToUpdate, final Predicate predicate) throws Exception {
    final String tableName =
        entityMappingHolder.tableToEntityNameMap.inverse().get(entity.getClass().getSimpleName());
    final LinkedHashMap<String, ColumnInfo> columnInfoMap =
        entityMappingHolder.columnInfoPerTable.get(tableName);

    final StringBuilder sqlBuilder = new StringBuilder("UPDATE " + tableName + " SET ");
    String delim = "";
    final List<Pair<String, Object>> parametersList = new ArrayList<>();
    for (final ColumnInfo columnInfo : columnInfoMap.values()) {
      final String columnNameInDB = columnInfo.getColumnNameInDB();
      if (!AUTO_UPDATE_COLUMN_SET.contains(columnNameInDB)
          && (fieldsToUpdate == null || fieldsToUpdate.contains(columnInfo.getColumnNameInEntity()))) {
        Object val = columnInfo.getField().get(entity);
        if (val != null) {
          if (Enum.class.isAssignableFrom(val.getClass())) {
            val = val.toString();
          }
          sqlBuilder.append(delim);
          sqlBuilder.append(columnNameInDB);
          sqlBuilder.append("=");
          sqlBuilder.append("?");
          delim = ",";
          parametersList.add(new ImmutablePair<>(columnNameInDB, val));
        }
      }
    }
    final BiMap<String, String> entityNameToDBNameMapping =
        entityMappingHolder.columnMappingPerTable.get(tableName).inverse();
    final StringBuilder whereClause = new StringBuilder(" WHERE ");
    generateWhereClause(entityNameToDBNameMapping, predicate, parametersList, whereClause);
    sqlBuilder.append(whereClause);
    int parameterIndex = 1;
    final PreparedStatement prepareStatement = connection.prepareStatement(sqlBuilder.toString());
    for (final Pair<String, Object> paramEntry : parametersList) {
      final String dbFieldName = paramEntry.getKey();
      final ColumnInfo info = columnInfoMap.get(dbFieldName);
      prepareStatement.setObject(parameterIndex++, paramEntry.getValue(), info.getSqlType());
    }
    return prepareStatement;
  }

  public PreparedStatement createDeleteStatement(final Connection connection,
      final Class<? extends AbstractEntity> entityClass,
      final Predicate predicate) throws Exception {
    if (predicate == null || predicate.getOper() == null) {
      throw new IllegalArgumentException("Predicate to delete cannot be null/empty");
    }
    final String tableName =
        entityMappingHolder.tableToEntityNameMap.inverse().get(entityClass.getSimpleName());
    final LinkedHashMap<String, ColumnInfo> columnInfoMap =
        entityMappingHolder.columnInfoPerTable.get(tableName);
    final StringBuilder sqlBuilder = new StringBuilder("DELETE FROM " + tableName);
    final BiMap<String, String> entityNameToDBNameMapping =
        entityMappingHolder.columnMappingPerTable.get(tableName).inverse();
    List<Pair<String, Object>> parametersList = new ArrayList<>();
    StringBuilder whereClause = new StringBuilder(" WHERE ");
    generateWhereClause(entityNameToDBNameMapping, predicate, parametersList, whereClause);
    sqlBuilder.append(whereClause);
    final PreparedStatement prepareStatement = connection.prepareStatement(sqlBuilder.toString());
    int parameterIndex = 1;
    for (final Pair<String, Object> paramEntry : parametersList) {
      final String dbFieldName = paramEntry.getKey();
      final ColumnInfo info = columnInfoMap.get(dbFieldName);
      prepareStatement.setObject(parameterIndex++, paramEntry.getValue(), info.getSqlType());
    }
    return prepareStatement;
  }

  public PreparedStatement createFindByParamsStatementWithLimit(final Connection connection,
      final Class<? extends AbstractEntity> entityClass, final Predicate predicate, final Long limit, final Long offset)
      throws Exception {
    final String tableName = entityMappingHolder.tableToEntityNameMap.inverse()
        .get(entityClass.getSimpleName());
    final StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName);
    final List<Pair<String, Object>> parametersList = new ArrayList<>();
    if(predicate != null) {
      final BiMap<String, String> entityNameToDBNameMapping =
          entityMappingHolder.columnMappingPerTable.get(tableName).inverse();
      final StringBuilder whereClause = new StringBuilder(" WHERE ");
      generateWhereClause(entityNameToDBNameMapping, predicate, parametersList, whereClause);
      sqlBuilder.append(whereClause);
    }
    if (limit != null) {
      sqlBuilder.append(" LIMIT ").append(limit);
    }
    if (offset != null) {
      sqlBuilder.append(" OFFSET ").append(offset);
    }
    final PreparedStatement prepareStatement = connection.prepareStatement(sqlBuilder.toString());
    if (!parametersList.isEmpty()) {
      int parameterIndex = 1;
      final LinkedHashMap<String, ColumnInfo> columnInfoMap =
          entityMappingHolder.columnInfoPerTable.get(tableName);
      for (final Pair<String, Object> pair : parametersList) {
        final String dbFieldName = pair.getKey();
        final ColumnInfo info = columnInfoMap.get(dbFieldName);
        checkNotNull(info,
            String.format("Found field '%s' but expected %s", dbFieldName, columnInfoMap.keySet()));
        prepareStatement.setObject(parameterIndex++, pair.getValue(), info.getSqlType());
      }
    }
    return prepareStatement;
  }

  public PreparedStatement createCountStatement(final Connection connection, final Predicate predicate,
      final Class<? extends AbstractIndexEntity> indexEntityClass) throws Exception {
    final String tableName =
        entityMappingHolder.tableToEntityNameMap.inverse().get(indexEntityClass.getSimpleName());
    final BiMap<String, String> entityNameToDBNameMapping =
        entityMappingHolder.columnMappingPerTable.get(tableName).inverse();

    final StringBuilder sqlBuilder = new StringBuilder("SELECT count(*) FROM " + tableName);
    final List<Pair<String, Object>> parametersList = new ArrayList<>();

    if(predicate != null) {
      final StringBuilder whereClause = new StringBuilder(" WHERE ");
      generateWhereClause(entityNameToDBNameMapping,
          predicate,
          parametersList,
          whereClause);
      sqlBuilder.append(whereClause);
    }
    final PreparedStatement preparedStatement = connection.prepareStatement(sqlBuilder.toString());
    if (!parametersList.isEmpty()) {
      int parameterIndex = 1;
      final Map<String, ColumnInfo> columnInfoMap =
          entityMappingHolder.columnInfoPerTable.get(tableName);

      for (final Pair<String, Object> pair : parametersList) {
        final String dbFieldName = pair.getKey();
        final ColumnInfo info = columnInfoMap.get(dbFieldName);
        checkNotNull(info,
            String.format("Found field '%s' but expected %s", dbFieldName, columnInfoMap.keySet()));
        preparedStatement.setObject(parameterIndex++, pair.getValue(), info.getSqlType());
      }
    }
    return preparedStatement;
  }

  private void generateWhereClause(final BiMap<String, String> entityNameToDBNameMapping,
      final Predicate predicate, final List<Pair<String, Object>> parametersList, final StringBuilder whereClause) {
    String columnName = null;

    if (predicate.getLhs() != null) {
      columnName = entityNameToDBNameMapping.get(predicate.getLhs());
      checkNotNull(columnName, String
          .format("Found field '%s' but expected %s", predicate.getLhs(),
              entityNameToDBNameMapping.keySet()));
    }

    switch (predicate.getOper()) {
      case AND:
      case OR:
        whereClause.append("(");
        String delim = "";
        for (final Predicate childPredicate : predicate.getChildPredicates()) {
          whereClause.append(delim);
          generateWhereClause(entityNameToDBNameMapping, childPredicate, parametersList,
              whereClause);
          delim = "  " + predicate.getOper().toString() + " ";
        }
        whereClause.append(")");
        break;
      case EQ:
      case LIKE:
      case GT:
      case LT:
      case NEQ:
      case LE:
      case GE:
        whereClause.append(columnName).append(" ").append(predicate.getOper().toString())
            .append(" ?");
        parametersList.add(ImmutablePair.of(columnName, predicate.getRhs()));
        break;
      case IN:
        Object rhs = predicate.getRhs();
        if (rhs != null) {
          if (!rhs.getClass().isArray()) {
            rhs = rhs.toString().split(",");
          }
          whereClause.append(columnName).append(" ").append(Predicate.OPER.IN)
              .append("(");
          delim = "";
          final int length = Array.getLength(rhs);
          if (length > 0) {
            for (int i = 0; i < length; i++) {
              whereClause.append(delim).append("?");
              parametersList.add(ImmutablePair.of(columnName, Array.get(rhs, i)));
              delim = ",";
            }
          } else {
            whereClause.append("null");
          }
          whereClause.append(")");
        }
        break;
      case BETWEEN:
        whereClause.append(columnName).append(predicate.getOper().toString()).append("? AND ?");
        final ImmutablePair<Object, Object> pair = (ImmutablePair<Object, Object>) predicate.getRhs();
        parametersList.add(ImmutablePair.of(columnName, pair.getLeft()));
        parametersList.add(ImmutablePair.of(columnName, pair.getRight()));
        break;
      default:
        throw new RuntimeException("Unsupported predicate type:" + predicate.getOper());
    }
  }

  public PreparedStatement createStatementFromSQL(final Connection connection, String parameterizedSQL,
      final Map<String, Object> parameterMap, final Class<? extends AbstractEntity> entityClass)
      throws Exception {
    final String tableName =
        entityMappingHolder.tableToEntityNameMap.inverse().get(entityClass.getSimpleName());
    parameterizedSQL = "select * from " + tableName + " " + parameterizedSQL;
    parameterizedSQL = parameterizedSQL.replace(entityClass.getSimpleName(), tableName);
    final StringBuilder psSql = new StringBuilder();
    final List<String> paramNames = new ArrayList<>();
    final Matcher m = PARAM_PATTERN.matcher(parameterizedSQL);

    int index = 0;
    while (m.find(index)) {
      psSql.append(parameterizedSQL, index, m.start());
      final String name = m.group(1);
      index = m.end();
      if (parameterMap.containsKey(name)) {
        psSql.append("?");
        paramNames.add(name);
      } else {
        throw new IllegalArgumentException(
            "Unknown parameter '" + name + "' at position " + m.start());
      }
    }

    // Any stragglers?
    psSql.append(parameterizedSQL.substring(index));
    String sql = psSql.toString();
    final BiMap<String, String> dbNameToEntityNameMapping =
        entityMappingHolder.columnMappingPerTable.get(tableName);
    for (final Entry<String, String> entry : dbNameToEntityNameMapping.entrySet()) {
      final String dbName = entry.getKey();
      final String entityName = entry.getValue();
      sql = sql.replaceAll(entityName, dbName);
    }
    final PreparedStatement ps = connection.prepareStatement(sql);
    int parameterIndex = 1;
    final LinkedHashMap<String, ColumnInfo> columnInfo =
        entityMappingHolder.columnInfoPerTable.get(tableName);
    for (final String entityFieldName : paramNames) {
      final String[] entityFieldNameParts = entityFieldName.split("__", 2);
      final String dbFieldName = dbNameToEntityNameMapping.inverse().get(entityFieldNameParts[0]);

      Object val = parameterMap.get(entityFieldName);
      if (Enum.class.isAssignableFrom(val.getClass())) {
        val = val.toString();
      }
      ps.setObject(parameterIndex++, val, columnInfo.get(dbFieldName).getSqlType());
    }

    return ps;
  }
}
