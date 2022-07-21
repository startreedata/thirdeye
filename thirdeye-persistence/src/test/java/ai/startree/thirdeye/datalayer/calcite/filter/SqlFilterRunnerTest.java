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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.datalayer.calcite.object.adapter.EventToRelationAdapter;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 * All tests are performed with the EventToRelationAdapter - this should generalize well
 */
public class SqlFilterRunnerTest {

  private static final String TYPE_HOLIDAY = "HOLIDAY";
  private static final String TYPE_CUSTOM = "CUSTOM";

  private static final String COUNTRY_DIMENSION_KEY = "country";
  private static final String US_COUNTRY_VALUE = "US";
  private static final String FR_COUNTRY_VALUE = "FR";

  private static final String ENV_DIMENSION_KEY = "environment";
  private static final String PROD_ENV_VALUE = "prod";
  private static final String DEV_ENV_VALUE = "dev";
  private static final Map<String, List<String>> DIMENSIONS = Map.of(
      COUNTRY_DIMENSION_KEY,
      List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
      ENV_DIMENSION_KEY,
      List.of(PROD_ENV_VALUE));

  private static final EventDTO CHRISTMAS_EVENT = (EventDTO) new EventDTO()
      .setName("CHRISTMAS")
      .setEventType(TYPE_HOLIDAY)
      .setTargetDimensionMap(DIMENSIONS)
      .setId(1L);
  private static final EventDTO EASTER_EVENT = (EventDTO) new EventDTO().setName("EASTER")
      .setEventType(TYPE_HOLIDAY)
      .setTargetDimensionMap(DIMENSIONS)
      .setId(2L);
  private static final EventDTO FR_ONLY_EVENT = (EventDTO) new EventDTO().setName("FR_ONLY_EVENT")
      .setEventType(TYPE_CUSTOM)
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(PROD_ENV_VALUE)))
      .setId(3L);
  private static final EventDTO DEV_ENV_ONLY_EVENT = (EventDTO) new EventDTO().setName(
          "DEV_ENV_ONLY_EVENT")
      .setEventType(TYPE_CUSTOM)
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(DEV_ENV_VALUE, "otherLongNoMatchValue")))
      .setId(4L);

  private static final List<EventDTO> EVENT_LIST = List.of(CHRISTMAS_EVENT,
      EASTER_EVENT,
      FR_ONLY_EVENT,
      DEV_ENV_ONLY_EVENT);

  private static final SqlFilterRunner<EventDTO> FILTER_RUNNER = new SqlFilterRunner<>(new EventToRelationAdapter());

  @Test
  public void testEmptyOrBlankFilterReturnsInput() {
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, "")).isEqualTo(EVENT_LIST);
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, " ")).isEqualTo(EVENT_LIST);
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, null)).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testElementWithNullIdThrows() {
    // second element does not have an id - should fail
    final List<EventDTO> events = List.of(
        CHRISTMAS_EVENT,
        new EventDTO().setEventType(TYPE_HOLIDAY).setName("malformedElement"),
        FR_ONLY_EVENT
    );
    assertThatThrownBy(() -> FILTER_RUNNER.applyFilter(events, "true")).isInstanceOf(
        IllegalArgumentException.class);
  }

  @Test
  public void testApplyFilterWithTruePredicate() {
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, "true")).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testApplyFilterWithFalsePredicate() {
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, "false")).isEqualTo(List.of());
  }

  @Test
  public void testApplyFilterWithTypeFilter() {
    final String sqlFiler = "type='HOLIDAY'";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, sqlFiler)).isEqualTo(List.of(CHRISTMAS_EVENT,
        EASTER_EVENT));
  }

  @Test
  public void testApplyFilterWithTypeInFilterList() {
    final String sqlFiler = "type in ('HOLIDAY', 'CUSTOM')";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, sqlFiler)).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testApplyFilterWithDimensionFilter() {
    final String sqlFiler = "'US' member of dimensionMap['country']";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, sqlFiler)).isEqualTo(List.of(
        CHRISTMAS_EVENT,
        EASTER_EVENT,
        DEV_ENV_ONLY_EVENT
    ));
  }

  @Test
  public void testApplyFilterWithComplexDimensionAndTypeFilter() {
    final String sqlFiler = "'US' member of dimensionMap['country'] OR (type = 'CUSTOM' and 'prod' member of dimensionMap['environment'])";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST, sqlFiler)).isEqualTo(List.of(
        CHRISTMAS_EVENT,
        EASTER_EVENT,
        FR_ONLY_EVENT,
        DEV_ENV_ONLY_EVENT
    ));
  }

  @Test
  public void testApplyFilterWithSetOfPossibleValues() {
    final String sqlFiler = "(MULTISET['dev', 'abc'] MULTISET INTERSECT dimensionMap['environment']) is not empty";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST,
        sqlFiler)).isEqualTo(List.of(DEV_ENV_ONLY_EVENT));
  }

  @Test()
  @Ignore
  // fixme cyril breaks because if the left MULTISET contains strings with different length - looks like a Calcite issue
  // see discussion https://lists.apache.org/thread/dfhmklr15gp7dm2wd09xhvb5ntb895zm
  public void testApplyFilterWithSetOfPossibleValuesBroken() {
    final String sqlFiler = "(MULTISET['dev', 'abcde'] MULTISET INTERSECT dimensionMap['environment']) is not empty";
    assertThat(FILTER_RUNNER.applyFilter(EVENT_LIST,
        sqlFiler)).isEqualTo(List.of(DEV_ENV_ONLY_EVENT));
  }


}
