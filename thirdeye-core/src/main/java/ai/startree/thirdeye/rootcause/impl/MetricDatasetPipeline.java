/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.rootcause.MaxScoreSet;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import ai.startree.thirdeye.spi.rootcause.impl.DatasetEntity;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline for identifying relevant metrics based on dataset
 * association. The pipeline first fetches metric entities from the context and then
 * searches Thirdeye's internal database for metrics contained in the same datasets as
 * any metric entities in the search context. All found metrics are scored equally.
 */
public class MetricDatasetPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MetricDatasetPipeline.class);

  private enum MappingDirection {
    METRIC_TO_DATASET,
    DATASET_TO_METRIC
  }

  private static final String PROP_COEFFICIENT = "coefficient";
  private static final double PROP_COEFFICIENT_DEFAULT = 1.0;

  private static final String PROP_DIRECTION = "direction";
  private static final String PROP_DIRECTION_DEFAULT = MappingDirection.METRIC_TO_DATASET
      .toString();

  public static final String META_METRIC_COUNT = "__COUNT";

  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private double coefficient;
  private MappingDirection direction;

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();
    this.metricDAO = context.getMetricConfigManager();
    this.datasetDAO = context.getDatasetConfigManager();
    this.coefficient = MapUtils
        .getDoubleValue(properties, PROP_COEFFICIENT, PROP_COEFFICIENT_DEFAULT);
    this.direction = MappingDirection.valueOf(MapUtils.getString(properties, PROP_DIRECTION, PROP_DIRECTION_DEFAULT));
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    if (MappingDirection.METRIC_TO_DATASET.equals(this.direction)) {
      // metric to dataset
      Set<MetricEntity> metrics = context.filter(MetricEntity.class);
      return new PipelineResult(context, metrics2datasets(metrics));
    } else {
      // dataset to metric
      Set<DatasetEntity> datasets = context.filter(DatasetEntity.class);
      return new PipelineResult(context, datasets2metrics(datasets));
    }
  }

  private Set<MetricEntity> datasets2metrics(Iterable<DatasetEntity> datasets) {
    Set<MetricEntity> entities = new MaxScoreSet<>();
    for (DatasetEntity de : datasets) {
      DatasetConfigDTO dataset = datasetDAO.findByDataset(de.getName());
      if (dataset == null) {
        LOG.warn("Could not find dataset '{}'", de.getName());
        continue;
      }

      Collection<MetricConfigDTO> dtos = metricDAO.findByDataset(de.getName());
      dtos = removeInactive(dtos);
      dtos = removeMeta(dtos);

      for (MetricConfigDTO dto : dtos) {
        entities.add(MetricEntity
            .fromMetric(de.getScore() * coefficient, Collections.singleton(de), dto.getId()));
      }
    }

    return entities;
  }

  private Set<DatasetEntity> metrics2datasets(Iterable<MetricEntity> metrics) {
    Set<DatasetEntity> entities = new MaxScoreSet<>();
    for (MetricEntity me : metrics) {
      MetricConfigDTO metricDTO = this.metricDAO.findById(me.getId());
      entities.add(DatasetEntity.fromName(me.getScore() * coefficient, Collections.singleton(me),
          metricDTO.getDataset()));
    }
    return entities;
  }

  static Collection<MetricConfigDTO> removeMeta(Iterable<MetricConfigDTO> dtos) {
    Collection<MetricConfigDTO> out = new ArrayList<>();
    for (MetricConfigDTO dto : dtos) {
      if (dto.getName().endsWith(META_METRIC_COUNT)) {
        continue;
      }
      out.add(dto);
    }
    return out;
  }

  static Collection<MetricConfigDTO> removeExisting(Iterable<MetricConfigDTO> dtos,
      Iterable<MetricEntity> existing) {
    Collection<MetricConfigDTO> out = new ArrayList<>();
    for (MetricConfigDTO dto : dtos) {
      if (!findExisting(dto, existing)) {
        out.add(dto);
      }
    }
    return out;
  }

  static Collection<MetricConfigDTO> removeInactive(Iterable<MetricConfigDTO> dtos) {
    Collection<MetricConfigDTO> out = new ArrayList<>();
    for (MetricConfigDTO dto : dtos) {
      if (dto.isActive()) {
        out.add(dto);
      }
    }
    return out;
  }

  static boolean findExisting(MetricConfigDTO dto, Iterable<MetricEntity> existing) {
    for (MetricEntity me : existing) {
      if (me.getId() == dto.getId()) {
        return true;
      }
    }
    return false;
  }
}
