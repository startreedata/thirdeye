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

package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.api.cube.DimensionCost;
import ai.startree.thirdeye.spi.api.cube.SummaryGainerLoserResponseRow;
import ai.startree.thirdeye.spi.api.cube.SummaryResponseRow;
import java.util.ArrayList;
import java.util.List;

public class DimensionAnalysisResultApi {

  private MetricApi metric;
  private Double baselineTotal = 0d;
  private Double currentTotal = 0d;
  private Double baselineTotalSize = 0d;
  private Double currentTotalSize = 0d;
  private Double globalRatio = 1d;
  private List<String> dimensions = new ArrayList<>();
  private List<SummaryResponseRow> responseRows = new ArrayList<>();
  private List<SummaryGainerLoserResponseRow> gainer = new ArrayList<>();
  private List<SummaryGainerLoserResponseRow> loser = new ArrayList<>();
  private List<DimensionCost> dimensionCosts = new ArrayList<>();

  public MetricApi getMetric() {
    return metric;
  }

  public DimensionAnalysisResultApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public Double getBaselineTotal() {
    return baselineTotal;
  }

  public DimensionAnalysisResultApi setBaselineTotal(final Double baselineTotal) {
    this.baselineTotal = baselineTotal;
    return this;
  }

  public Double getCurrentTotal() {
    return currentTotal;
  }

  public DimensionAnalysisResultApi setCurrentTotal(final Double currentTotal) {
    this.currentTotal = currentTotal;
    return this;
  }

  public Double getBaselineTotalSize() {
    return baselineTotalSize;
  }

  public DimensionAnalysisResultApi setBaselineTotalSize(final Double baselineTotalSize) {
    this.baselineTotalSize = baselineTotalSize;
    return this;
  }

  public Double getCurrentTotalSize() {
    return currentTotalSize;
  }

  public DimensionAnalysisResultApi setCurrentTotalSize(final Double currentTotalSize) {
    this.currentTotalSize = currentTotalSize;
    return this;
  }

  public Double getGlobalRatio() {
    return globalRatio;
  }

  public DimensionAnalysisResultApi setGlobalRatio(final Double globalRatio) {
    this.globalRatio = globalRatio;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public DimensionAnalysisResultApi setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public List<SummaryResponseRow> getResponseRows() {
    return responseRows;
  }

  public DimensionAnalysisResultApi setResponseRows(
      final List<SummaryResponseRow> responseRows) {
    this.responseRows = responseRows;
    return this;
  }

  public List<SummaryGainerLoserResponseRow> getGainer() {
    return gainer;
  }

  public DimensionAnalysisResultApi setGainer(
      final List<SummaryGainerLoserResponseRow> gainer) {
    this.gainer = gainer;
    return this;
  }

  public List<SummaryGainerLoserResponseRow> getLoser() {
    return loser;
  }

  public DimensionAnalysisResultApi setLoser(
      final List<SummaryGainerLoserResponseRow> loser) {
    this.loser = loser;
    return this;
  }

  public List<DimensionCost> getDimensionCosts() {
    return dimensionCosts;
  }

  public DimensionAnalysisResultApi setDimensionCosts(
      final List<DimensionCost> dimensionCosts) {
    this.dimensionCosts = dimensionCosts;
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(DimensionAnalysisResultApi.class.getSimpleName());
    sb.append("\n\t").append(this.getDimensions());
    for (SummaryResponseRow row : getResponseRows()) {
      sb.append("\n\t").append(row);
    }
    return sb.toString();
  }
}
