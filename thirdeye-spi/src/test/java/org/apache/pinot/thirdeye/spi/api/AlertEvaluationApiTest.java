package org.apache.pinot.thirdeye.spi.api;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.util.GroovyTemplateUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertEvaluationApiTest {

  @Test
  public void testAlertEvaluationPlan() throws IOException, ClassNotFoundException {
    final ClassLoader classLoader = AlertEvaluationApiTest.class.getClassLoader();
    URL resource = requireNonNull(classLoader.getResource("alertEvaluation.json"));
    final String jsonString = Resources.toString(resource, StandardCharsets.UTF_8);
    resource = classLoader.getResource("alertEvaluation-context.json");
    final Map<String, Object> alertEvaluationPlanApiContext = new ObjectMapper()
        .readValue(resource.openStream(), Map.class);

    final AlertEvaluationApi api = GroovyTemplateUtils.applyContextToTemplate(
        jsonString,
        alertEvaluationPlanApiContext);

    Assert.assertEquals(api.getAlert().getName(), "percentage-change-template");
    Assert.assertEquals(api.getAlert().getDescription(),
        "Percentage drop template");
    Assert.assertEquals(api.getAlert().getCron(), "0 0/1 * 1/1 * ? *");
    Assert.assertEquals(api.getStart(), new Date(1621300000));
    Assert.assertEquals(api.getEnd(), new Date(1621200000));

    assertThat(api.getAlert()).isNotNull();
    assertThat(api.getAlert().getTemplate()).isNotNull();

    final List<PlanNodeApi> nodes = api.getAlert().getTemplate().getNodes();
    Assert.assertEquals(nodes.size(), 5);
    Assert.assertEquals(nodes.get(0).getName(), "root");
    Assert.assertEquals(nodes.get(1).getName(),
        "percentageChangeDetector");
    Assert.assertEquals(nodes.get(2).getName(),
        "baselineDataFetcher");
    Assert.assertEquals(nodes.get(3).getName(),
        "currentDataFetcher");
    Assert.assertEquals(nodes.get(4).getName(), "sqlJoin");
    Assert.assertEquals(nodes.get(0).getType(), "ChildKeepingMerge");
    Assert.assertEquals(nodes.get(1).getType(), "AnomalyDetector");
    Assert.assertEquals(nodes.get(2).getType(), "DataFetcher");
    Assert.assertEquals(nodes.get(3).getType(), "DataFetcher");
    Assert.assertEquals(nodes.get(4).getType(), "SqlQueryExecutor");
  }
}
