/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.annotations.Test;

public class PinotQueryExecutorTest {

  public static final String BASE_QUERY = "SELECT * from table;";

  @Test
  public void testbuildQueryWithOptionsZeroOptions() {
    final Map<String, String> zeroOptions = Map.of();
    final PinotQuery pinotQuery = new PinotQuery(BASE_QUERY, null, zeroOptions);
    final String output = PinotQueryExecutor.buildQueryWithOptions(pinotQuery);

    assertThat(output).isEqualTo(BASE_QUERY);
  }

  @Test
  public void testbuildQueryWithOptionsOneOptions() {
    final Map<String, String> oneOption = Map.of("timeoutMs", "10000");
    final String expectedOptionString = "SET timeoutMs = 10000;";

    final PinotQuery pinotQuery = new PinotQuery(BASE_QUERY, null, oneOption);
    final String output = PinotQueryExecutor.buildQueryWithOptions(pinotQuery);

    assertThat(output).isEqualTo(expectedOptionString+BASE_QUERY);
  }

  @Test
  public void testbuildQueryWithOptionsTwoOptions() {
    final Map<String, String> twoOptions = new LinkedHashMap<>();
    twoOptions.put("timeoutMs", "10000");
    twoOptions.put("stringOption", "'stringValue'");
    final String expectedOptionString = "SET timeoutMs = 10000;SET stringOption = 'stringValue';";

    final PinotQuery pinotQuery = new PinotQuery(BASE_QUERY, null, twoOptions);
    final String output = PinotQueryExecutor.buildQueryWithOptions(pinotQuery);

    assertThat(output).isEqualTo(expectedOptionString+BASE_QUERY);

  }
}
