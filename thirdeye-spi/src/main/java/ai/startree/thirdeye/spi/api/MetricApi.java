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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.datalayer.dto.LogicalView;
import ai.startree.thirdeye.spi.metric.MetricType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class MetricApi implements ThirdEyeCrudApi<MetricApi> {

  private Long id;
  private String name;
  @Deprecated
  private String urn;
  private DatasetApi dataset;
  private Boolean active;
  private Date created;
  private Date updated;
  private String derivedMetricExpression;
  private String aggregationColumn;
  private MetricType datatype;
  private String aggregationFunction;
  @Deprecated
  @JsonIgnore
  private Double rollupThreshold;
  private List<LogicalView> views;
  private String where;

  public Long getId() {
    return id;
  }

  public MetricApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public MetricApi setName(final String name) {
    this.name = name;
    return this;
  }

  @Deprecated
  public String getUrn() {
    return urn;
  }

  @Deprecated
  public MetricApi setUrn(final String urn) {
    this.urn = urn;
    return this;
  }

  public DatasetApi getDataset() {
    return dataset;
  }

  public MetricApi setDataset(final DatasetApi dataset) {
    this.dataset = dataset;
    return this;
  }

  public Boolean getActive() {
    return active;
  }

  public MetricApi setActive(final Boolean active) {
    this.active = active;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public MetricApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public MetricApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public String getDerivedMetricExpression() {
    return derivedMetricExpression;
  }

  public MetricApi setDerivedMetricExpression(final String derivedMetricExpression) {
    this.derivedMetricExpression = derivedMetricExpression;
    return this;
  }

  public String getAggregationColumn() {
    return aggregationColumn;
  }

  public MetricApi setAggregationColumn(String aggregationColumn) {
    this.aggregationColumn = aggregationColumn;
    return this;
  }

  public MetricType getDatatype() {
    return datatype;
  }

  public MetricApi setDatatype(final MetricType datatype) {
    this.datatype = datatype;
    return this;
  }

  public String getAggregationFunction() {
    return aggregationFunction;
  }

  public MetricApi setAggregationFunction(final String aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
    return this;
  }

  public List<LogicalView> getViews() {
    return views;
  }

  public MetricApi setViews(List<LogicalView> views) {
    this.views = views;
    return this;
  }

  public String getWhere() {
    return where;
  }

  public MetricApi setWhere(String where) {
    this.where = where;
    return this;
  }
}
