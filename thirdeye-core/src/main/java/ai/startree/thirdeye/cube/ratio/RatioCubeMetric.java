/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.ratio;

import ai.startree.thirdeye.cube.data.dbclient.CubeFetcherImpl;
import ai.startree.thirdeye.cube.data.dbclient.CubeMetric;
import ai.startree.thirdeye.cube.data.dbclient.CubeSpec;
import ai.startree.thirdeye.cube.data.dbclient.CubeTag;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;

/**
 * Describes a CubeMetric that is a simple ratio metric such as "A/B" where A and B is a metric.
 *
 * @see CubeFetcherImpl
 */
public class RatioCubeMetric implements CubeMetric<RatioRow> {

  private final DatasetConfigDTO datasetConfigDTO;
  private final MetricConfigDTO numeratorMetricName;
  private final MetricConfigDTO denominatorMetricName;
  private final Interval currentInterval;
  private final Interval baselineInterval;

  /**
   * Constructs an Additive cube metric.
   */
  public RatioCubeMetric(final DatasetConfigDTO datasetConfigDTO,
      final MetricConfigDTO numeratorMetricName,
      final MetricConfigDTO denominatorMetricName,
      final Interval currentInterval,
      final Interval baselineInterval) {
    this.datasetConfigDTO = datasetConfigDTO;
    this.numeratorMetricName = numeratorMetricName;
    this.denominatorMetricName = denominatorMetricName;
    this.currentInterval = currentInterval;
    this.baselineInterval = baselineInterval;
  }

  @Override
  public DatasetConfigDTO getDataset() {
    return datasetConfigDTO;
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
