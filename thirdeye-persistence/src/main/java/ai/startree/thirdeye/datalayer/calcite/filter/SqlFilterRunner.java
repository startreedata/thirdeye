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
package ai.startree.thirdeye.datalayer.calcite.filter;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_CALCITE_FILTERING;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_INVALID_SQL;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datalayer.calcite.object.ObjectSchema;
import ai.startree.thirdeye.spi.ThirdEyeException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.RelRunners;
import org.apache.calcite.tools.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Run sql filter predicates on any list of objects with a given ObjectWithIdToRelationAdapter
 */
public class SqlFilterRunner<T> {

  private final ObjectWithIdToRelationAdapter<T> adapter;

  public SqlFilterRunner(final ObjectWithIdToRelationAdapter<T> adapter) {
    this.adapter = adapter;
  }

  public List<T> applyFilter(@NonNull final List<T> elements, @Nullable final String queryFilter) {
    if (StringUtils.isBlank(queryFilter) || elements.isEmpty()) {
      return elements;
    }

    for (T e : elements) {
      checkArgument(adapter.idOf(e) != null,
          "An element has a null id. All elements passed to SqlFilterRunner must have a non-null id. Element: %s",
          e);
    }

    final ObjectSchema<T> objectSchema = new ObjectSchema<>(elements, adapter);
    final SchemaPlus querySchema = Frameworks.createRootSchema(true)
        .add(objectSchema.singleTableName(), objectSchema);
    final Planner planner = buildPlanner(querySchema);
    final String sqlQuery = buildQueryString(queryFilter, objectSchema.singleTableName());
    try {
      final ResultSet resultSet = runQuery(planner, sqlQuery);
      final Set<Long> matchingIds = getIdsFrom(resultSet);
      //return the original collection filtered by the matching ids
      return elements.stream()
          .filter(e -> matchingIds.contains(adapter.idOf(e)))
          .collect(Collectors.toList());
    } catch (RelConversionException | SQLException | ValidationException e) {
      throw new ThirdEyeException(e, ERR_CALCITE_FILTERING, queryFilter);
    }
  }

  @NonNull
  private Set<Long> getIdsFrom(final ResultSet resultSet) throws SQLException {
    final Set<Long> matchingIds = new HashSet<>();
    while (resultSet.next()) {
      final Long id = resultSet.getLong(adapter.idColumn());
      matchingIds.add(id);
    }
    return matchingIds;
  }

  private static ResultSet runQuery(final Planner planner, final String sqlQuery)
      throws ValidationException, RelConversionException, SQLException {
    final SqlNode sqlNode;
    try {
      sqlNode = planner.parse(sqlQuery);
    } catch (SqlParseException e) {
      throw new ThirdEyeException(e, ERR_INVALID_SQL, sqlQuery);
    }
    final SqlNode sqlNodeValidated = planner.validate(sqlNode);
    final RelRoot relRoot = planner.rel(sqlNodeValidated);
    final RelNode relNode = relRoot.project();
    final PreparedStatement run = RelRunners.run(relNode);
    return run.executeQuery();
  }

  @NonNull
  private String buildQueryString(final String queryFilter, final String tableName) {
    final String cleanTextPredicate = cleanFreeTextPredicate(
        queryFilter);

    return "select " + adapter.idColumn() + " from " + tableName + " WHERE " + cleanTextPredicate;
  }

  @NonNull
  private static Planner buildPlanner(final SchemaPlus querySchema) {
    final SqlParser.Config insensitiveParser = SqlParser.config().withCaseSensitive(false);
    final FrameworkConfig config = Frameworks.newConfigBuilder()
        .sqlToRelConverterConfig(SqlToRelConverter.config().withExpand(false))
        .parserConfig(insensitiveParser)
        .defaultSchema(querySchema)
        .build();

    return Frameworks.getPlanner(config);
  }

  // fixme cyril - duplicated - move calcite classes to a new module and consolidate
  @NonNull
  public static String cleanFreeTextPredicate(final String freeTextPredicate) {
    return freeTextPredicate.replaceFirst("^ *[aA][nN][dD] +", "");
  }
}
