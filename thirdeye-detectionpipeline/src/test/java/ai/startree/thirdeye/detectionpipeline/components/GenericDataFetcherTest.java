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
package ai.startree.thirdeye.detectionpipeline.components;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.metric.DimensionType;
import org.testng.annotations.Test;

public class GenericDataFetcherTest {

  public static final String TABLE_NAME = "tableName";
  public static final String RHS = "value1";
  public static final String LHS = "dim1";

  @Test
  public void testToQueryPredicate() {
    final GenericDataFetcher genericDataFetcher = new GenericDataFetcher();
    genericDataFetcher.setTableName(TABLE_NAME);
    final Predicate predicate = new Predicate(LHS, OPER.EQ, RHS);
    final QueryPredicate res = genericDataFetcher.toQueryPredicate(predicate);

    assertThat(res.getPredicate()).isEqualTo(predicate);
    assertThat(res.getDataset()).isEqualTo(TABLE_NAME);
    assertThat(res.getMetricType()).isEqualTo(DimensionType.STRING);
  }
}
