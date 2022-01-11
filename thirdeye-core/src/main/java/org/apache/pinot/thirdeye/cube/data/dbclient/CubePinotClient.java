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

package org.apache.pinot.thirdeye.cube.data.dbclient;

import org.apache.pinot.thirdeye.cube.data.dbrow.Row;
import org.joda.time.Interval;

/**
 * The CubeClient that is based on Pinot.
 */
public interface CubePinotClient<R extends Row> extends CubeClient<R> {

  /**
   * Sets Pinot dataset.
   *
   * @param dataset the dataset.
   */
  void setDataset(String dataset);

  /**
   * Sets the baseline interval.
   *
   * @param baselineInterval the interval of the baseline.
   */
  void setBaselineInterval(Interval baselineInterval);

  /**
   * Sets the start date time of current (inclusive).
   *
   * @param currentInterval the interval of the current data.
   */
  void setCurrentInterval(Interval currentInterval);
}
