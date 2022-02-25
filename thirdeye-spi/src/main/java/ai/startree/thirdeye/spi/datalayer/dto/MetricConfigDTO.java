/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.spi.detection.metric.MetricType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricConfigDTO extends AbstractDTO {

  public static final String ALIAS_JOINER = "::";
  public static final String METRIC_PROPERTIES_SEPARATOR = ",";

  private String name;
  private String dataset;
  private String alias;
  private Set<String> tags;
  private MetricType datatype;
  private String derivedMetricExpression;
  private String aggregationColumn;
  private MetricAggFunction defaultAggFunction;
  @Deprecated
  @JsonIgnore // not used anymore
  private Double rollupThreshold;
  private boolean inverseMetric = false;
  private String cellSizeExpression;
  private boolean active = true;
  private Map<String, String> extSourceLinkInfo;
  private Map<String, String> extSourceLinkTimeGranularity;
  private Map<String, String> metricProperties = null;
  private boolean dimensionAsMetric = false;
  private List<LogicalView> views;
  private String where;

  @JsonIgnore
  private DatasetConfigDTO datasetConfig;

  public DatasetConfigDTO getDatasetConfig() {
    return datasetConfig;
  }

  public void setDatasetConfig(DatasetConfigDTO datasetConfig) {
    this.datasetConfig = datasetConfig;
  }

  public String getName() {
    return name;
  }

  public MetricConfigDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDataset() {
    return dataset;
  }

  public MetricConfigDTO setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getAlias() {
    return alias;
  }

  public MetricConfigDTO setAlias(final String alias) {
    this.alias = alias;
    return this;
  }

  public Set<String> getTags() {
    return tags;
  }

  public MetricConfigDTO setTags(final Set<String> tags) {
    this.tags = tags;
    return this;
  }

  public MetricType getDatatype() {
    return datatype;
  }

  public MetricConfigDTO setDatatype(final MetricType datatype) {
    this.datatype = datatype;
    return this;
  }

  public String getDerivedMetricExpression() {
    return derivedMetricExpression;
  }

  public MetricConfigDTO setDerivedMetricExpression(final String derivedMetricExpression) {
    this.derivedMetricExpression = derivedMetricExpression;
    return this;
  }

  public String getAggregationColumn() {
    return aggregationColumn;
  }

  public MetricConfigDTO setAggregationColumn(String aggregationColumn) {
    this.aggregationColumn = aggregationColumn;
    return this;
  }

  public MetricAggFunction getDefaultAggFunction() {
    return defaultAggFunction;
  }

  public MetricConfigDTO setDefaultAggFunction(
      final MetricAggFunction defaultAggFunction) {
    this.defaultAggFunction = defaultAggFunction;
    return this;
  }

  public boolean isInverseMetric() {
    return inverseMetric;
  }

  public MetricConfigDTO setInverseMetric(final boolean inverseMetric) {
    this.inverseMetric = inverseMetric;
    return this;
  }

  public String getCellSizeExpression() {
    return cellSizeExpression;
  }

  public MetricConfigDTO setCellSizeExpression(final String cellSizeExpression) {
    this.cellSizeExpression = cellSizeExpression;
    return this;
  }

  public boolean isActive() {
    return active;
  }

  public MetricConfigDTO setActive(final boolean active) {
    this.active = active;
    return this;
  }

  public Map<String, String> getExtSourceLinkInfo() {
    return extSourceLinkInfo;
  }

  public MetricConfigDTO setExtSourceLinkInfo(
      final Map<String, String> extSourceLinkInfo) {
    this.extSourceLinkInfo = extSourceLinkInfo;
    return this;
  }

  public Map<String, String> getExtSourceLinkTimeGranularity() {
    return extSourceLinkTimeGranularity;
  }

  public MetricConfigDTO setExtSourceLinkTimeGranularity(
      final Map<String, String> extSourceLinkTimeGranularity) {
    this.extSourceLinkTimeGranularity = extSourceLinkTimeGranularity;
    return this;
  }

  public Map<String, String> getMetricProperties() {
    return metricProperties;
  }

  public MetricConfigDTO setMetricProperties(
      final Map<String, String> metricProperties) {
    this.metricProperties = metricProperties;
    return this;
  }

  public boolean isDimensionAsMetric() {
    return dimensionAsMetric;
  }

  public MetricConfigDTO setDimensionAsMetric(final boolean dimensionAsMetric) {
    this.dimensionAsMetric = dimensionAsMetric;
    return this;
  }

  public List<LogicalView> getViews() {
    return views;
  }

  public MetricConfigDTO setViews(List<LogicalView> views) {
    this.views = views;
    return this;
  }

  public String getWhere() {
    return where;
  }

  public MetricConfigDTO setWhere(String where) {
    this.where = where;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MetricConfigDTO)) {
      return false;
    }
    MetricConfigDTO mc = (MetricConfigDTO) o;
    return Objects.equals(getId(), mc.getId())
        && Objects.equals(name, mc.getName())
        && Objects.equals(dataset, mc.getDataset())
        && Objects.equals(alias, mc.getAlias())
        && Objects.equals(derivedMetricExpression, mc.getDerivedMetricExpression())
        && Objects.equals(defaultAggFunction, mc.getDefaultAggFunction())
        && Objects.equals(dimensionAsMetric, mc.isDimensionAsMetric())
        && Objects.equals(inverseMetric, mc.isInverseMetric())
        && Objects.equals(cellSizeExpression, mc.getCellSizeExpression())
        && Objects.equals(active, mc.isActive())
        && Objects.equals(extSourceLinkInfo, mc.getExtSourceLinkInfo())
        && Objects.equals(metricProperties, mc.getMetricProperties())
        && Objects.equals(views, mc.getViews())
        && Objects.equals(where, mc.getWhere());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getId(),
            dataset,
            alias,
            derivedMetricExpression,
            defaultAggFunction,
            inverseMetric,
            cellSizeExpression,
            active,
            extSourceLinkInfo,
            metricProperties,
            views,
            where);
  }

  /**
   * Properties to set in metricProperties, in order to express a metric as a metricAsDimension
   * case.
   *
   * eg: If a dataset has columns as follows:
   * counter_name - a column which stores the name of the metric being expressed in that row
   * counter_value - a column which stores the metric value corresponding to the counter_name in
   * that row
   * If we are interested in a metric called records_processed,
   * it means we are interested in counter_value from rows which have
   * counter_name=records_processed
   * Thus the metric definition would be
   * {
   * name : "RecordsProcessed",
   * dataset : "myDataset",
   * dimensionAsMetric : true,
   * metricProperties : {
   * "METRIC_NAMES" : "records_processed",
   * "METRIC_NAMES_COLUMNS" : "counter_name",
   * "METRIC_VALUES_COLUMN" : "counter_value"
   * }
   * }
   *
   * eg: If a dataset has columns as follows:
   * counter_name_primary - a column which stores the name of the metric being expressed in that
   * row
   * counter_name_secondary - another column which stores the name of the metric being expressed in
   * that row
   * counter_value - a column which stores the metric value corresponding to the
   * counter_name_primary and counter_name_secondary in that row
   * If we are interested in a metric called primary=records_processed secondary=internal,
   * it means we are interested in counter_value from rows which have
   * counter_name_primary=records_processed and counter_name_secondary=internal
   * Thus the metric definition would be
   * {
   * name : "RecordsProcessedInternal",
   * dataset : "myDataset",
   * dimensionAsMetric : true,
   * metricProperties : {
   * "METRIC_NAMES" : "records_processed,internal",
   * "METRIC_NAMES_COLUMNS" : "counter_name_primary,counter_name_secondary",
   * "METRIC_VALUES_COLUMN" : "counter_value"
   * }
   * }
   */
  public enum DimensionAsMetricProperties {
    /**
     * The actual names of the metrics, comma separated
     */
    METRIC_NAMES,
    /**
     * The columns in which to look for the metric names, comma separated
     */
    METRIC_NAMES_COLUMNS,
    /**
     * The column from which to get the metric value
     */
    METRIC_VALUES_COLUMN
  }
}
