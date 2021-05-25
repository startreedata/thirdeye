package org.apache.pinot.thirdeye.api.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertEvaluationPlanApiTest {

  @Test
  public void testAlertEvaluationPlan() throws IOException, ClassNotFoundException {
    final ClassLoader classLoader = AlertEvaluationPlanApiTest.class.getClassLoader();
    URL resource = classLoader.getResource("alertEvaluation.json");
    String alertEvaluationPlanApiTemplate = IOUtils.toString(resource);
    resource = classLoader.getResource("alertEvaluation-context.json");
    Map<String, Object> alertEvaluationPlanApiContext = new ObjectMapper().readValue(resource.openStream(),
        Map.class);

    final AlertEvaluationPlanApi alertEvaluationPlanApi = AlertEvaluationPlanApi.applyContextToTemplate(
        alertEvaluationPlanApiTemplate,
        alertEvaluationPlanApiContext);

    Assert.assertEquals(alertEvaluationPlanApi.getAlert().getName(), "percentage-change-template");
    Assert.assertEquals(alertEvaluationPlanApi.getAlert().getDescription(),
        "Percentage drop template");
    Assert.assertEquals(alertEvaluationPlanApi.getAlert().getCron(), "0 0/1 * 1/1 * ? *");
    Assert.assertEquals(alertEvaluationPlanApi.getStart(), new Date(1621300000));
    Assert.assertEquals(alertEvaluationPlanApi.getEnd(), new Date(1621200000));
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().size(), 5);
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(0).getPlanNodeName(), "root");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(1).getPlanNodeName(),
        "percentageChangeDetector");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(2).getPlanNodeName(),
        "baselineDataFetcher");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(3).getPlanNodeName(),
        "currentDataFetcher");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(4).getPlanNodeName(), "sqlJoin");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(0).getType(), "ChildKeepingMerge");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(1).getType(), "AnomalyDetector");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(2).getType(), "DataFetcher");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(3).getType(), "DataFetcher");
    Assert.assertEquals(alertEvaluationPlanApi.getNodes().get(4).getType(), "SqlQueryExecutor");
  }
}
