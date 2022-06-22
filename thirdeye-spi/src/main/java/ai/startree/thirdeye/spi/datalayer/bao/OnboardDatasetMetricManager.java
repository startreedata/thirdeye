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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import java.util.List;

public interface OnboardDatasetMetricManager extends AbstractManager<OnboardDatasetMetricDTO> {

  List<OnboardDatasetMetricDTO> findByDataSource(String dataSource);

  List<OnboardDatasetMetricDTO> findByDataSourceAndOnboarded(String dataSource, boolean onboarded);

  List<OnboardDatasetMetricDTO> findByDataset(String datasetName);

  List<OnboardDatasetMetricDTO> findByMetric(String metricName);

  List<OnboardDatasetMetricDTO> findByDatasetAndDatasource(String datasetName, String dataSource);
}
