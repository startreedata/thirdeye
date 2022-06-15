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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.alert.AlertEvaluator.EVALUATION_FILTERS_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.detectionpipeline.plan.AnomalyDetectorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.IndexFillerPlanNode;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.spi.metric.DimensionType;
import java.util.HashMap;
import java.util.List;
import org.testng.annotations.Test;

public class AlertEvaluatorTest {

  private static final String DATASET_NAME = "dataset";
  private static final String ANOMALY_DETECTOR_TYPE = new AnomalyDetectorPlanNode().getType();
  private static final String INDEX_FILLER_TYPE = new IndexFillerPlanNode().getType();
  private static final String DATA_FETCHER_TYPE = new DataFetcherPlanNode().getType();

  @Test
  public void testInjectFilters() {
    AlertEvaluator evaluatorV2 = new AlertEvaluator(null, null, null);
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(DATASET_NAME);
    AlertTemplateDTO alertTemplateDTO = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO))
        .setNodes(List.of(
            new PlanNodeBean().setName("root").setType(ANOMALY_DETECTOR_TYPE),
            new PlanNodeBean().setName("indexFiller1").setType(INDEX_FILLER_TYPE),
            new PlanNodeBean().setName("indexFiller2").setType(INDEX_FILLER_TYPE)
            .setParams(new HashMap<>()),
            new PlanNodeBean().setName("dataFetcher1").setType(DATA_FETCHER_TYPE),
            new PlanNodeBean().setName("dataFetcher2").setType(DATA_FETCHER_TYPE)
                .setParams(new HashMap<>())
        ));
    List<String> filters = List.of("browser=chrome");

    evaluatorV2.injectFilters(alertTemplateDTO, filters);

    // test that filters are not injected in detector and index filler nodes
    assertThat(alertTemplateDTO.getNodes().get(0).getParams()).isNull();
    assertThat(alertTemplateDTO.getNodes().get(1).getParams()).isNull();
    assertThat(alertTemplateDTO.getNodes().get(2).getParams()).isNotNull();
    assertThat(alertTemplateDTO.getNodes().get(2).getParams().get(EVALUATION_FILTERS_KEY)).isNull();

    // test that filters are injected in data fetcher nodes
    assertThat(alertTemplateDTO.getNodes().get(3).getParams().get(EVALUATION_FILTERS_KEY)).isNotNull();
    assertThat(alertTemplateDTO.getNodes().get(4).getParams().get(EVALUATION_FILTERS_KEY)).isNotNull();

    // check the filter value for one data fetcher
    List<QueryPredicate> injectedFilters = (List<QueryPredicate>) alertTemplateDTO
        .getNodes().get(3).getParams().get(EVALUATION_FILTERS_KEY);
    assertThat(injectedFilters.size()).isEqualTo(1);
    assertThat(injectedFilters.get(0).getDataset()).isEqualTo(DATASET_NAME);
    assertThat(injectedFilters.get(0).getMetricType()).isEqualTo(DimensionType.STRING);
    assertThat(injectedFilters.get(0).getPredicate()).isEqualTo(new Predicate("browser", OPER.EQ, "chrome"));
  }
}
