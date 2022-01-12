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

package org.apache.pinot.thirdeye.cube.ratio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeFetcherImpl;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeMetric;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeSpec;
import org.apache.pinot.thirdeye.cube.data.dbclient.CubeTag;
import org.apache.pinot.thirdeye.cube.data.dbrow.DimensionValues;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.joda.time.Interval;

/**
 * Describes a CubeMetric that is a simple ratio metric such as "A/B" where A and B is a metric.
 *
 * @see CubeFetcherImpl
 */
public class RatioCubeMetric implements CubeMetric<RatioRow> {

  private final String dataset;
  private final String numeratorMetricName;
  private final String denominatorMetricName;
  private final Interval currentInterval;
  private final Interval baselineInterval;

  /**
   * Constructs an Additive cube metric.
   */
  public RatioCubeMetric(String dataset,
      String numeratorMetricName,
      String denominatorMetricName,
      Interval currentInterval,
      Interval baselineInterval) {
    this.dataset = dataset;
    this.numeratorMetricName = numeratorMetricName;
    this.denominatorMetricName = denominatorMetricName;
    this.currentInterval = currentInterval;
    this.baselineInterval = baselineInterval;
  }

  @Override
  public String getDataset() {
    return dataset;
  }

  @Override
  public String getMetric() {
    return numeratorMetricName + "/" + denominatorMetricName;
  }

  @Override
  public List<CubeSpec> getCubeSpecs() {
    List<CubeSpec> cubeSpecs = new ArrayList<>();

    cubeSpecs.add(
        new CubeSpec(CubeTag.BaselineNumerator, numeratorMetricName, baselineInterval));
    cubeSpecs.add(
        new CubeSpec(CubeTag.BaselineDenominator, denominatorMetricName, baselineInterval));
    cubeSpecs.add(new CubeSpec(CubeTag.CurrentNumerator, numeratorMetricName, currentInterval));
    cubeSpecs.add(
        new CubeSpec(CubeTag.CurrentDenominator, denominatorMetricName, currentInterval));

    return cubeSpecs;
  }

  @Override
  public void fillValueToRowTable(Map<List<String>, RatioRow> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag) {

    if (Double.compare(0d, value) < 0 && !Double.isInfinite(value)) {
      RatioRow row = rowTable.get(dimensionValues);
      if (row == null) {
        row = new RatioRow(dimensions, new DimensionValues(dimensionValues));
        rowTable.put(dimensionValues, row);
      }
      switch (tag) {
        case BaselineNumerator:
          row.setBaselineNumeratorValue(value);
          break;
        case BaselineDenominator:
          row.setBaselineDenominatorValue(value);
          break;
        case CurrentNumerator:
          row.setCurrentNumeratorValue(value);
          break;
        case CurrentDenominator:
          row.setCurrentDenominatorValue(value);
          break;
        default:
          throw new IllegalArgumentException("Unsupported CubeTag: " + tag.name());
      }
    }
  }
}
