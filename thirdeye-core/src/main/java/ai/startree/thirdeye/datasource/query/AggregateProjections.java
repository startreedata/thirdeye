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
package ai.startree.thirdeye.datasource.query;

import static ai.startree.thirdeye.spi.datasource.loader.AggregationLoader.COL_AGGREGATION_ONLY_ROWS_COUNT;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Container class for common query projections.
 */
public class AggregateProjections {

  public static QueryProjection aggProjection(@Nullable final String operator,
      final List<String> operands) {
    return new QueryProjection(operator, operands, null, null, false);
  }

  /**
   * Creates an aggregation projection based on a metricConfig.
   */
  public static QueryProjection aggProjection(final MetricConfigDTO metricConfigDTO) {
    final String aggFunction = Objects.requireNonNull(metricConfigDTO.getDefaultAggFunction());
    final List<String> operands = "*".equals(metricConfigDTO.getName())
        ? List.of("*")
        : List.of(QueryProjection.getColName(metricConfigDTO));

    return aggProjection(aggFunction, operands);
  }

  public static QueryProjection countStar() {
    return aggProjection("COUNT", List.of("*"))
        .withAlias(COL_AGGREGATION_ONLY_ROWS_COUNT);
  }
}
