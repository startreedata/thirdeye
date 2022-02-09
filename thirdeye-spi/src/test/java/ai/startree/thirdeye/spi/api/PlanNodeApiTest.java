package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlanNodeApiTest {

  @Test
  public void testInputNode() throws IOException {
    URL resource = PlanNodeApiTest.class.getClassLoader().getResource("inputNode.json");
    final PlanNodeApi inputNode = new ObjectMapper().readValue(
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
    final PlanNodeApi inputNode = new ObjectMapper().readValue(Resources.toString(resource,
        StandardCharsets.UTF_8),
        PlanNodeApi.class);
    Assert.assertEquals(inputNode.getName(), "percentageChangeDetector");
    Assert.assertEquals(inputNode.getType(), "AnomalyDetector");
    Assert.assertEquals(inputNode.getParams().size(), 6);
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
