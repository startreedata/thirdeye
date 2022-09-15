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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.metric.DimensionType;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationContextProcessor {

  public void process(final AlertTemplateDTO templateWithProperties,
      final EvaluationContextApi evaluationContext) {

    final List<String> filters = evaluationContext.getFilters();
    if (filters != null) {
      injectFilters(templateWithProperties, filters);
    }
  }

  @VisibleForTesting
  protected void injectFilters(final AlertTemplateDTO templateWithProperties,
      final List<String> filters) {
    if (filters.isEmpty()) {
      return;
    }
    final AlertMetadataDTO alertMetadataDTO = ensureExists(templateWithProperties.getMetadata(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata");
    final DatasetConfigDTO datasetConfigDTO = ensureExists(alertMetadataDTO.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset");
    final String dataset = ensureExists(datasetConfigDTO.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset$name");

    final List<QueryPredicate> timeseriesFilters = parseAndCombinePredicates(filters).stream()
        .map(p -> QueryPredicate.of(p, getDimensionType(p.getLhs(), dataset), dataset))
        .collect(Collectors.toList());

    templateWithProperties.getNodes().forEach(n -> addFilters(n, timeseriesFilters));
  }

  // fixme datatype from metricDTO is always double + abstraction metric/dimension needs refactoring
  private DimensionType getDimensionType(final String metric, final String dataset) {
    // first version: assume dimension is always of type String
    // todo fetch info from database with a DAO
    return DimensionType.STRING;
  }

  private void addFilters(final PlanNodeBean planNodeBean, final List<QueryPredicate> filters) {
    if (planNodeBean.getType().equals(new DataFetcherPlanNode().getType())) {
      if (planNodeBean.getParams() == null) {
        planNodeBean.setParams(new TemplatableMap<>());
      }
      planNodeBean.getParams().putValue(Constants.EVALUATION_FILTERS_KEY, filters);
    }
  }
}
