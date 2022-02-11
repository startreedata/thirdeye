/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.spec.MockTunableSpec;
import ai.startree.thirdeye.detection.spi.components.Tunable;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import java.util.Collections;
import java.util.Map;
import org.joda.time.Interval;

public class MockTunableDetector implements
    Tunable<MockTunableSpec> {

  private int tuneRunes = 0;

  @Override
  public void init(final MockTunableSpec spec) {

  }

  @Override
  public void init(MockTunableSpec spec, InputDataFetcher dataFetcher) {
    // left empty
  }

  public int getTuneRuns() {
    return tuneRunes;
  }

  @Override
  public Map<String, Object> tune(Map<String, Object> currentSpec, Interval tuningWindow,
      String metricUrn) {
    this.tuneRunes++;
    return Collections.emptyMap();
  }
}
