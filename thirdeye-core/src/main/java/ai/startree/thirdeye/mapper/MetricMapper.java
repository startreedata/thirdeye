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
package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.util.SpiUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MetricMapper {

  MetricMapper INSTANCE = Mappers.getMapper(MetricMapper.class);

  default MetricConfigDTO toBean(MetricApi api) {
    if (api == null) {
      return null;
    }
    final MetricConfigDTO dto = new MetricConfigDTO();
    String alias = null;
    if (api.getName() != null && api.getDataset() != null && api.getDataset().getName() != null) {
      alias = SpiUtils.constructMetricAlias(api.getDataset().getName(), api.getName());
    }
    dto.setId(api.getId());
    dto
        .setName(api.getName())
        .setAlias(alias)
        .setDataset(optional(api.getDataset())
            .map(DatasetApi::getName)
            .orElse(null))
        .setAggregationColumn(api.getAggregationColumn())
        .setDatatype(api.getDatatype())
        .setDefaultAggFunction(api.getAggregationFunction())
        .setActive(api.getActive())
        .setViews(api.getViews())
        .setWhere(api.getWhere())
        .setDerivedMetricExpression(api.getDerivedMetricExpression());

    return dto;
  }

  default MetricApi toApi(MetricConfigDTO dto) {
    if (dto == null) {
      return null;
    }
    return new MetricApi()
        .setId(dto.getId())
        .setActive(dto.getActive())
        .setName(dto.getName())
        .setUpdated(dto.getUpdateTime())
        .setDataset(optional(dto.getDataset())
            .map(datasetName -> new DatasetApi().setName(datasetName)).orElse(null))
        .setDerivedMetricExpression(dto.getDerivedMetricExpression())
        .setWhere(dto.getWhere())
        .setAggregationColumn(dto.getAggregationColumn())
        .setDatatype(dto.getDatatype())
        .setAggregationFunction(dto.getDefaultAggFunction())
        .setViews(dto.getViews())
        ;
  }
}
