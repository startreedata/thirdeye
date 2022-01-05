/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.apache.pinot.thirdeye.detection.components;

import java.util.Collections;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.spec.MockTunableSpec;
import org.apache.pinot.thirdeye.detection.spi.components.Tunable;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
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
