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
package ai.startree.thirdeye.spi.metric;

import static ai.startree.thirdeye.spi.metric.MetricAggFunction.parsePercentile;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class MetricAggFunctionTest {

  @Test
  public void testparsePercentile() {
    assertThat(parsePercentile("pct95")).isEqualTo(95);
    assertThat(parsePercentile("pct999")).isEqualTo(99.9);
    assertThat(parsePercentile("pct999999")).isEqualTo(99.9999);
    assertThat(parsePercentile("pct05")).isEqualTo(5);

    // invalid strings
    assertThat(parsePercentile("pct5")).isNull();
    assertThat(parsePercentile("pct")).isNull();
    assertThat(parsePercentile("95")).isNull();
    assertThat(parsePercentile("bla95")).isNull();
  }
}
