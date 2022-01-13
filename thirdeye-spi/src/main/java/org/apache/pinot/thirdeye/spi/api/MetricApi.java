package org.apache.pinot.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import org.apache.pinot.thirdeye.spi.datalayer.dto.LogicalView;
import org.apache.pinot.thirdeye.spi.detection.MetricAggFunction;
import org.apache.pinot.thirdeye.spi.detection.metric.MetricType;

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
  private MetricAggFunction aggregationFunction;
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

  public MetricAggFunction getAggregationFunction() {
    return aggregationFunction;
  }

  public MetricApi setAggregationFunction(final MetricAggFunction aggregationFunction) {
    this.aggregationFunction = aggregationFunction;
    return this;
  }

  public Double getRollupThreshold() {
    return rollupThreshold;
  }

  public MetricApi setRollupThreshold(final Double rollupThreshold) {
    this.rollupThreshold = rollupThreshold;
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
