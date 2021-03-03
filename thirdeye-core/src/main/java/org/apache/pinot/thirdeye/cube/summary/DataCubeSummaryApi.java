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

package org.apache.pinot.thirdeye.cube.summary;

import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.cube.data.cube.Cube;
import org.apache.pinot.thirdeye.cube.data.cube.Cube.DimensionCost;

public class DataCubeSummaryApi {

  private String metricUrn;
  private String dataset;
  private String metricName;
  private double baselineTotal = 0d;
  private double currentTotal = 0d;
  private double baselineTotalSize = 0d;
  private double currentTotalSize = 0d;
  private double globalRatio = 1d;
  private List<String> dimensions = new ArrayList<>();
  private List<SummaryResponseRow> responseRows = new ArrayList<>();
  private List<SummaryGainerLoserResponseRow> gainer = new ArrayList<>();
  private List<SummaryGainerLoserResponseRow> loser = new ArrayList<>();
  private List<Cube.DimensionCost> dimensionCosts = new ArrayList<>();

  public String getMetricUrn() {
    return metricUrn;
  }

  public DataCubeSummaryApi setMetricUrn(final String metricUrn) {
    this.metricUrn = metricUrn;
    return this;
  }

  public String getDataset() {
    return dataset;
  }

  public DataCubeSummaryApi setDataset(final String dataset) {
    this.dataset = dataset;
    return this;
  }

  public String getMetricName() {
    return metricName;
  }

  public DataCubeSummaryApi setMetricName(final String metricName) {
    this.metricName = metricName;
    return this;
  }

  public double getBaselineTotal() {
    return baselineTotal;
  }

  public DataCubeSummaryApi setBaselineTotal(final double baselineTotal) {
    this.baselineTotal = baselineTotal;
    return this;
  }

  public double getCurrentTotal() {
    return currentTotal;
  }

  public DataCubeSummaryApi setCurrentTotal(final double currentTotal) {
    this.currentTotal = currentTotal;
    return this;
  }

  public double getBaselineTotalSize() {
    return baselineTotalSize;
  }

  public DataCubeSummaryApi setBaselineTotalSize(final double baselineTotalSize) {
    this.baselineTotalSize = baselineTotalSize;
    return this;
  }

  public double getCurrentTotalSize() {
    return currentTotalSize;
  }

  public DataCubeSummaryApi setCurrentTotalSize(final double currentTotalSize) {
    this.currentTotalSize = currentTotalSize;
    return this;
  }

  public double getGlobalRatio() {
    return globalRatio;
  }

  public DataCubeSummaryApi setGlobalRatio(final double globalRatio) {
    this.globalRatio = globalRatio;
    return this;
  }

  public List<String> getDimensions() {
    return dimensions;
  }

  public DataCubeSummaryApi setDimensions(final List<String> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public List<SummaryResponseRow> getResponseRows() {
    return responseRows;
  }

  public DataCubeSummaryApi setResponseRows(
      final List<SummaryResponseRow> responseRows) {
    this.responseRows = responseRows;
    return this;
  }

  public List<SummaryGainerLoserResponseRow> getGainer() {
    return gainer;
  }

  public DataCubeSummaryApi setGainer(
      final List<SummaryGainerLoserResponseRow> gainer) {
    this.gainer = gainer;
    return this;
  }

  public List<SummaryGainerLoserResponseRow> getLoser() {
    return loser;
  }

  public DataCubeSummaryApi setLoser(
      final List<SummaryGainerLoserResponseRow> loser) {
    this.loser = loser;
    return this;
  }

  public List<DimensionCost> getDimensionCosts() {
    return dimensionCosts;
  }

  public DataCubeSummaryApi setDimensionCosts(
      final List<DimensionCost> dimensionCosts) {
    this.dimensionCosts = dimensionCosts;
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(DataCubeSummaryApi.class.getSimpleName());
    sb.append("\n\t").append(this.getDimensions());
    for (SummaryResponseRow row : getResponseRows()) {
      sb.append("\n\t").append(row);
    }
    return sb.toString();
  }
}
