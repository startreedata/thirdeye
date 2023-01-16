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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import java.util.Set;

public interface MetricConfigManager extends AbstractManager<MetricConfigDTO> {

  List<MetricConfigDTO> findByDataset(String dataset);

  MetricConfigDTO findByMetricAndDataset(String metricName, String dataset);

  List<MetricConfigDTO> findActiveByDataset(String dataset);

  List<MetricConfigDTO> findWhereNameOrAliasLikeAndActive(String name);

  List<MetricConfigDTO> findWhereAliasLikeAndActive(Set<String> aliasParts);

  List<MetricConfigDTO> findByMetricName(String metricName);
}
