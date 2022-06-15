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
package ai.startree.thirdeye.resources;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertResourceTest {

  @Test
  public void testAlertEvaluationPlan() throws IOException, ClassNotFoundException {
    final ClassLoader classLoader = AlertResourceTest.class.getClassLoader();
    URL resource = requireNonNull(classLoader.getResource("alertEvaluation.json"));
    final String jsonString = Resources.toString(resource, StandardCharsets.UTF_8);
    resource = classLoader.getResource("alertEvaluation-context.json");
    final Map<String, Object> alertEvaluationPlanApiContext = new ObjectMapper()
        .readValue(resource.openStream(), Map.class);

    final AlertEvaluationApi api = new ObjectMapper().readValue(StringTemplateUtils.renderTemplate(
        jsonString,
        alertEvaluationPlanApiContext), AlertEvaluationApi.class);

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
