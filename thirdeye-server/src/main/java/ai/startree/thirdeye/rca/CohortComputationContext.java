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

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import java.util.List;
import org.apache.calcite.sql.SqlNode;
import org.joda.time.Interval;

public class CohortComputationContext {

  private MetricConfigDTO metric;
  private DatasetConfigDTO dataset;
  private ThirdEyeDataSource dataSource;

  private Interval interval;
  private List<String> allDimensions;
  private int limit = 100;
  private int maxDepth = 10;
  private SqlNode where;
  private SqlNode having;

  private Double threshold;
  private boolean roundOffThreshold = false;
  private Double aggregate;

  public MetricConfigDTO getMetric() {
    return metric;
  }

  public CohortComputationContext setMetric(final MetricConfigDTO metric) {
    this.metric = metric;
    return this;
  }

  public DatasetConfigDTO getDataset() {
    return dataset;
  }

  public CohortComputationContext setDataset(final DatasetConfigDTO dataset) {
    this.dataset = dataset;
    return this;
  }

  public ThirdEyeDataSource getDataSource() {
    return dataSource;
  }

  public CohortComputationContext setDataSource(final ThirdEyeDataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public Double getThreshold() {
    return threshold;
  }

  public CohortComputationContext setThreshold(final Double threshold) {
    this.threshold = threshold;
    return this;
  }

  public Double getAggregate() {
    return aggregate;
  }

  public CohortComputationContext setAggregate(final Double aggregate) {
    this.aggregate = aggregate;
    return this;
  }

  public Interval getInterval() {
    return interval;
  }

  public CohortComputationContext setInterval(final Interval interval) {
    this.interval = interval;
    return this;
  }

  public List<String> getAllDimensions() {
    return allDimensions;
  }

  public CohortComputationContext setAllDimensions(final List<String> allDimensions) {
    this.allDimensions = allDimensions;
    return this;
  }

  public int getLimit() {
    return limit;
  }

  public CohortComputationContext setLimit(final int limit) {
    this.limit = limit;
    return this;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public CohortComputationContext setMaxDepth(final int maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  public SqlNode getWhere() {
    return where;
  }

  public CohortComputationContext setWhere(final SqlNode where) {
    this.where = where;
    return this;
  }

  public SqlNode getHaving() {
    return having;
  }

  public CohortComputationContext setHaving(final SqlNode having) {
    this.having = having;
    return this;
  }

  public boolean isRoundOffThreshold() {
    return roundOffThreshold;
  }

  public CohortComputationContext setRoundOffThreshold(final boolean roundOffThreshold) {
    this.roundOffThreshold = roundOffThreshold;
    return this;
  }
}
