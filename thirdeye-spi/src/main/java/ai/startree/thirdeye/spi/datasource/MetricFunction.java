/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class MetricFunction implements Comparable<MetricFunction> {

  private MetricAggFunction functionName;
  private String metricName;
  private Long metricId;
  private String dataset;
  private MetricConfigDTO metricConfig;
  private DatasetConfigDTO datasetConfig;

  public MetricFunction() {

  }

  public MetricFunction(final MetricConfigDTO metricConfig, final DatasetConfigDTO datasetConfig) {
    this(MetricAggFunction.fromString(metricConfig.getDefaultAggFunction()),
        metricConfig.getName(),
        metricConfig.getId(),
        datasetConfig.getDataset(),
        metricConfig,
        datasetConfig);
  }

  @Deprecated
  public MetricFunction(
      @JsonProperty("functionName") MetricAggFunction functionName,
      @JsonProperty("metricName") String metricName,
      @JsonProperty("metricId") Long metricId,
      @JsonProperty("dataset") String dataset,
      @JsonProperty("metricConfig") MetricConfigDTO metricConfig,
      @JsonProperty("datasetConfig") DatasetConfigDTO datasetConfig) {
    this.functionName = functionName;
    this.metricName = metricName;
    this.metricId = metricId;
    this.dataset = dataset;
    this.metricConfig = metricConfig;
    this.datasetConfig = datasetConfig;
  }

  private String format(String functionName, String metricName) {
    return String.format("%s_%s", functionName, metricName);
  }

  @Override
  public String toString() {
    // TODO this is hardcoded for pinot's return column name, but there's no binding contract that
    // clients need to return response objects with these keys.
    return format(optional(functionName)
        .map(Enum::name)
        .orElse("null"), metricName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(functionName, metricName, metricId, dataset);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MetricFunction)) {
      return false;
    }
    MetricFunction mf = (MetricFunction) obj;
    return Objects.equal(functionName, mf.functionName)
        && Objects.equal(metricName, mf.metricName)
        && Objects.equal(metricId, mf.metricId)
        && Objects.equal(dataset, mf.dataset);
  }

  @Override
  public int compareTo(MetricFunction o) {
    return this.toString().compareTo(o.toString());
  }

  public MetricAggFunction getFunctionName() {
    return functionName;
  }

  public void setFunctionName(MetricAggFunction functionName) {
    this.functionName = functionName;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public Long getMetricId() {
    return metricId;
  }

  public void setMetricId(Long metricId) {
    this.metricId = metricId;
  }

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public MetricConfigDTO getMetricConfig() {
    return metricConfig;
  }

  public void setMetricConfig(MetricConfigDTO metricConfig) {
    this.metricConfig = metricConfig;
  }

  public DatasetConfigDTO getDatasetConfig() {
    return datasetConfig;
  }

  public void setDatasetConfig(DatasetConfigDTO datasetConfig) {
    this.datasetConfig = datasetConfig;
  }
}
