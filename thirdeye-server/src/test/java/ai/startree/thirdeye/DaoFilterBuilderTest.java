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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.DaoFilterBuilder.toPair;
import static ai.startree.thirdeye.DaoFilterBuilder.toPredicate;
import static ai.startree.thirdeye.spi.util.Pair.pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import com.google.common.collect.ImmutableMap;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.testng.annotations.Test;

public class DaoFilterBuilderTest {

  private DaoFilterBuilder builder() {
    return new DaoFilterBuilder(ImmutableMap.of());
  }

  private MultivaluedMap<String, String> queryParams(final String... args) {
    final MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
    for (int i=0; i < args.length; i += 2) {
      queryParams.add(args[i], args[i+1]);
    }
    return queryParams;
  }

  private void assertBadRequestException(final MultivaluedMap<String, String> queryParams) {
    assertThatThrownBy(() -> builder().buildFilter(queryParams))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  public void testToPair() {
    assertThat(toPair("abcd")).isEqualTo(pair(OPER.EQ, "abcd"));
    assertThat(toPair("[gt]1234")).isEqualTo(pair(OPER.GT, "1234"));
    assertThat(toPair("[gte]1234")).isEqualTo(pair(OPER.GE, "1234"));
    assertThat(toPair("[lte]-1")).isEqualTo(pair(OPER.LE, "-1"));
  }

  @Test
  public void testToOrPredicate() {
    assertThat(toPredicate("col", new Object[]{
        "1", "2", "3", "4"
    })).isEqualTo(Predicate.AND(
        Predicate.EQ("col", "1"),
        Predicate.EQ("col", "2"),
        Predicate.EQ("col", "3"),
        Predicate.EQ("col", "4")
    ));

    assertThat(toPredicate("col", new Object[]{
        "[gt]1", "[lte]-1"
    })).isEqualTo(Predicate.AND(
        Predicate.GT("col", "1"),
        Predicate.LE("col", "-1")
    ));
  }

  @Test
  public void testValidLimitOffsetParams() {
    final long limit = 5;
    assertThat(builder()
        .buildFilter(queryParams("limit", String.valueOf(limit)))
        .getLimit()
    ).isEqualTo(limit);

    final long offset = 10;
    assertThat(builder()
        .buildFilter(queryParams(
                "limit", String.valueOf(limit),
                "offset", String.valueOf(offset)))
        .getOffset()
    ).isEqualTo(offset);
  }

  @Test
  public void testNegativeLimitValue() {
    assertBadRequestException(queryParams("limit", "-1"));
  }

  @Test
  public void testNegativeOffsetValue() {
    assertBadRequestException(queryParams(
        "limit", "5",
        "offset", "-10"));
  }

  @Test
  public void testOffsetWithoutLimit() {
    assertBadRequestException(queryParams("offset", "10"));
  }
}
