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
package ai.startree.thirdeye.detection.detector.email.filter;

import ai.startree.thirdeye.datalayer.DaoTestUtils;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAlertFilterFactory {

  private static AlertFilterFactory alertFilterFactory;
  private static final String collection = "my dataset";
  private static final String metricName = "__counts";

  private TestAlertFilterFactory() {
    String mappingsPath = ClassLoader.getSystemResource("sample-alertfilter.properties").getPath();
    alertFilterFactory = new AlertFilterFactory(mappingsPath);
  }

  @Test
  public void fromSpecNullAlertFilter() throws Exception {
    AlertFilter alertFilter = alertFilterFactory.fromSpec(null);
    Assert.assertEquals(alertFilter.getClass(), DummyAlertFilter.class);
  }

  @Test
  public void testFromAnomalyFunctionSpecToAlertFilter() throws Exception {
    AnomalyFunctionDTO anomalyFunctionSpec = DaoTestUtils
        .getTestFunctionSpec(metricName, collection);
    AlertFilter alertFilter = alertFilterFactory.fromSpec(anomalyFunctionSpec.getAlertFilter());
    Assert.assertEquals(alertFilter.getClass(), DummyAlertFilter.class);

    anomalyFunctionSpec = DaoTestUtils
        .getTestFunctionAlphaBetaAlertFilterSpec(metricName, collection);
    alertFilter = alertFilterFactory.fromSpec(anomalyFunctionSpec.getAlertFilter());
    Assert.assertEquals(alertFilter.getClass(), AlphaBetaAlertFilter.class);
  }
}
