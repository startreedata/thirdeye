/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.additive;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.cube.data.dbclient.CubeFetcherImpl;
import ai.startree.thirdeye.cube.data.dbclient.CubeMetric;
import ai.startree.thirdeye.cube.data.dbclient.CubeSpec;
import ai.startree.thirdeye.cube.data.dbclient.CubeTag;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a CubeMetric that is additive.
 *
 * @see CubeFetcherImpl
 */
public class AdditiveCubeMetric implements CubeMetric<AdditiveRow> {

  private static final Logger LOG = LoggerFactory.getLogger(AdditiveCubeMetric.class);

  private final String dataset;
  private final String metricName;
  private final Interval currentInterval;
  private final Interval baselineInterval;

  /**
   * Constructs an Additive cube metric.
   */
  public AdditiveCubeMetric(
      String dataset,
      String metricName,
      Interval currentInterval,
      Interval baselineInterval) {
    checkArgument(!Strings.isNullOrEmpty(dataset));
    this.dataset = dataset;
    checkArgument(!Strings.isNullOrEmpty(metricName));
    this.metricName = metricName;
    this.currentInterval = Preconditions.checkNotNull(currentInterval);
    this.baselineInterval = Preconditions.checkNotNull(baselineInterval);
  }

  @Override
  public String getDataset() {
    return dataset;
  }

  @Override
  public String getMetric() {
    return metricName;
  }

  @Override
  public List<CubeSpec> getCubeSpecs() {
    List<CubeSpec> cubeSpecs = new ArrayList<>();

    cubeSpecs
        .add(new CubeSpec(CubeTag.Baseline, metricName, baselineInterval));
    cubeSpecs
        .add(new CubeSpec(CubeTag.Current, metricName, currentInterval));

    return cubeSpecs;
  }

  public void fillValueToRowTable(Map<List<String>, AdditiveRow> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag) {
    if (Double.compare(0d, value) >= 0) {
      LOG.warn("Value not added to rowTable: it is too small. Value: {}. Tag: {}", value, tag);
      return;
    }
    if (Double.isInfinite(value)) {
      LOG.warn("Value not added to rowTable: it is infinite. Value: {}. Tag: {}", value, tag);
      return;
    }
    AdditiveRow row = rowTable.get(dimensionValues);
    if (row == null) {
      row = new AdditiveRow(dimensions, new DimensionValues(dimensionValues));
      rowTable.put(dimensionValues, row);
    }
    switch (tag) {
      case Baseline:
        row.setBaselineValue(value);
        break;
      case Current:
        row.setCurrentValue(value);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CubeTag: " + tag.name());
    }
  }
}
