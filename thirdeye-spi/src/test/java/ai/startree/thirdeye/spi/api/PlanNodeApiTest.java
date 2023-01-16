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
package ai.startree.thirdeye.spi.api;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlanNodeApiTest {

  @Test
  public void testInputNode() throws IOException {
    URL resource = PlanNodeApiTest.class.getClassLoader().getResource("inputNode.json");
    final PlanNodeApi inputNode = ThirdEyeSerialization.getObjectMapper().readValue(
        Resources.toString(resource, StandardCharsets.UTF_8),
        PlanNodeApi.class);
    Assert.assertEquals(inputNode.getName(), "baselineDataFetcher");
    Assert.assertEquals(inputNode.getType(), "DataFetcher");
    Assert.assertEquals(inputNode.getParams().size(), 5);
    Assert.assertEquals(inputNode.getOutputs().size(), 1);
    Assert.assertEquals(inputNode.getOutputs().get(0).getOutputKey(), "output");
    Assert.assertEquals(inputNode.getOutputs().get(0).getOutputName(), "current");
  }

  @Test
  public void testDetectionNode() throws IOException {
    URL resource = PlanNodeApiTest.class.getClassLoader().getResource("detectionNode.json");
    final PlanNodeApi inputNode = ThirdEyeSerialization.getObjectMapper().readValue(Resources.toString(resource,
        StandardCharsets.UTF_8),
        PlanNodeApi.class);
    Assert.assertEquals(inputNode.getName(), "percentageChangeDetector");
    Assert.assertEquals(inputNode.getType(), "AnomalyDetector");
    Assert.assertEquals(inputNode.getParams().size(), 6);
    // parsing templatable with a templated value
    assertThat(inputNode.getParams().get("metric").templatedValue()).isEqualTo("${metricName}");
    // parsing templatable with an object value
    assertThat(inputNode.getParams().get("dimensions").value()).isEqualTo(List.of());
    // parsing templatable with a string value
    assertThat(inputNode.getParams().get("detectorName").value()).isEqualTo("PERCENTAGE_CHANGE");
    Assert.assertEquals(inputNode.getInputs().size(), 2);
    Assert.assertEquals(inputNode.getInputs().get(0).getTargetProperty(), "baseline");
    Assert.assertEquals(inputNode.getInputs().get(0).getSourcePlanNode(), "baselineDataFetcher");
    Assert.assertEquals(inputNode.getInputs().get(0).getSourceProperty(), "baselineOutput");
    Assert.assertEquals(inputNode.getInputs().get(1).getTargetProperty(), "current");
    Assert.assertEquals(inputNode.getInputs().get(1).getSourcePlanNode(), "currentDataFetcher");
    Assert.assertEquals(inputNode.getInputs().get(1).getSourceProperty(), "currentOutput");
    Assert.assertNull(inputNode.getOutputs());
  }
}
