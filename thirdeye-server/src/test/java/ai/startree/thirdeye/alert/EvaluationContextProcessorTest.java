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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.plan.AnomalyDetectorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.IndexFillerPlanNode;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import java.util.List;
import org.testng.annotations.Test;

public class EvaluationContextProcessorTest {

  private static final String DATASET_NAME = "dataset";
  private static final String ANOMALY_DETECTOR_TYPE = new AnomalyDetectorPlanNode().getType();
  private static final String INDEX_FILLER_TYPE = new IndexFillerPlanNode().getType();
  private static final String DATA_FETCHER_TYPE = new DataFetcherPlanNode().getType();

  @Test
  public void testInjectFilters() {
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO().setDataset(DATASET_NAME);
    AlertTemplateDTO alertTemplateDTO = new AlertTemplateDTO()
        .setMetadata(new AlertMetadataDTO().setDataset(datasetConfigDTO))
        .setNodes(List.of(
            new PlanNodeBean().setName("root").setType(ANOMALY_DETECTOR_TYPE),
            new PlanNodeBean().setName("indexFiller1").setType(INDEX_FILLER_TYPE),
            new PlanNodeBean().setName("indexFiller2").setType(INDEX_FILLER_TYPE)
                .setParams(new TemplatableMap<>()),
            new PlanNodeBean().setName("dataFetcher1").setType(DATA_FETCHER_TYPE),
            new PlanNodeBean().setName("dataFetcher2").setType(DATA_FETCHER_TYPE)
                .setParams(new TemplatableMap<>())
        ));
    final List<String> filters = List.of("browser=chrome");
    final EvaluationContextApi apiContext = new EvaluationContextApi().setFilters(filters);

    final PlanNodeContext res = new EvaluationContextProcessor().getContext(apiContext);

    assertThat(res.getPredicates()).isNotNull();
    assertThat(res.getPredicates().size()).isEqualTo(1);
    assertThat(res.getPredicates().get(0)).isEqualTo(new Predicate("browser", OPER.EQ, "chrome"));
  }
}
