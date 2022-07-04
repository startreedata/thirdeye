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
package ai.startree.thirdeye.spi.rca;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import org.joda.time.Interval;

public class ContributorsSearchConfiguration {

  private final MetricConfigDTO metricConfigDTO;
  private final DatasetConfigDTO datasetConfigDTO;
  private final Interval currentInterval;
  private final Interval baselineInterval;
  private final int summarySize;
  private final int depth;
  private final boolean doOneSideError;
  private final List<Predicate> filters;
  private final List<List<String>> hierarchies;

  public ContributorsSearchConfiguration(final MetricConfigDTO metricConfigDTO, final DatasetConfigDTO datasetConfigDTO,
      final Interval currentInterval, final Interval baselineInterval, final int summarySize,
      final int depth, final boolean doOneSideError, final List<Predicate> filters,
      final List<List<String>> hierarchies) {
    this.metricConfigDTO = metricConfigDTO;
    this.datasetConfigDTO = datasetConfigDTO;
    this.currentInterval = currentInterval;
    this.baselineInterval = baselineInterval;
    this.summarySize = summarySize;
    this.depth = depth;
    this.doOneSideError = doOneSideError;
    this.filters = filters;
    this.hierarchies = hierarchies;
  }

  public MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
  }

  public DatasetConfigDTO getDatasetConfigDTO() {
    return datasetConfigDTO;
  }

  public Interval getCurrentInterval() {
    return currentInterval;
  }

  public Interval getBaselineInterval() {
    return baselineInterval;
  }

  public int getSummarySize() {
    return summarySize;
  }

  public int getDepth() {
    return depth;
  }

  public boolean isDoOneSideError() {
    return doOneSideError;
  }

  public List<Predicate> getFilters() {
    return filters;
  }

  public List<List<String>> getHierarchies() {
    return hierarchies;
  }
}
