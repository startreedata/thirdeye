package org.apache.pinot.thirdeye.api.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DetectionPlanApiTest {

  @Test
  public void testInputNode() throws IOException {
    URL resource = DetectionPlanApiTest.class.getClassLoader().getResource("inputNode.json");
    final DetectionPlanApi inputNode = new ObjectMapper().readValue(IOUtils.toString(resource),
        DetectionPlanApi.class);
    Assert.assertEquals(inputNode.getPlanNodeName(), "baselineDataFetcher");
    Assert.assertEquals(inputNode.getType(), "DataFetcher");
    Assert.assertEquals(inputNode.getParams().size(), 5);
    Assert.assertEquals(inputNode.getOutputs().size(), 1);
    Assert.assertEquals(inputNode.getOutputs().get(0).getOutputKey(), "output");
    Assert.assertEquals(inputNode.getOutputs().get(0).getOutputName(), "current");
  }

  @Test
  public void testDetectionNode() throws IOException {
    URL resource = DetectionPlanApiTest.class.getClassLoader().getResource("detectionNode.json");
    final DetectionPlanApi inputNode = new ObjectMapper().readValue(IOUtils.toString(resource),
        DetectionPlanApi.class);
    Assert.assertEquals(inputNode.getPlanNodeName(), "percentageChangeDetector");
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
