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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetUtils;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PinotThirdEyeDataSourceTest {

  @Test
  public void testReduceSum() {
    Assert.assertEquals(ThirdEyeResultSetUtils.reduce(10, 3, 4, MetricAggFunction.SUM, "Pinot"),
        13.0);
  }

  @Test
  public void testReduceAvg() {
    Assert
        .assertEquals(ThirdEyeResultSetUtils.reduce(10, 2, 3, MetricAggFunction.AVG, "Pinot"), 8.0);
  }

  @Test
  public void testReduceMax() {
    Assert.assertEquals(ThirdEyeResultSetUtils.reduce(10, 3, 12, MetricAggFunction.MAX, "Pinot"),
        10.0);
  }

  @Test
  public void testReduceCount() {
    Assert.assertEquals(ThirdEyeResultSetUtils.reduce(4, 3, 4, MetricAggFunction.COUNT, "Pinot"),
        7.0);
  }

  @Test
  public void testReduceCountSQL() {
    Assert
        .assertEquals(ThirdEyeResultSetUtils.reduce(4, 3, 4, MetricAggFunction.COUNT, "SQL"), 7.0);
  }

  @Test
  public void testReduceTDigest() {
    Assert.assertEquals(ThirdEyeResultSetUtils.reduce(10, 2, 3, MetricAggFunction.PCT50, "Pinot"),
        8.0);
    Assert.assertEquals(ThirdEyeResultSetUtils.reduce(10, 2, 3, MetricAggFunction.PCT90, "Pinot"),
        8.0);
  }
}
