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

package ai.startree.thirdeye.detection.anomalydetection.function;

import ai.startree.thirdeye.detection.anomaly.views.AnomalyTimelinesView;
import ai.startree.thirdeye.detection.anomalydetection.context.AnomalyDetectionContext;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResult;
import java.util.List;
import org.joda.time.Interval;

/**
 * The anomaly detection function defines the necessary element for detection anomalies on a single
 * observed time series, i.e., an anomaly function does not handle an anomaly that is defined upon
 * time series from various metric and dimensions.
 */
public interface AnomalyDetectionFunction {

  /**
   * Initializes this function with its configuration, call before analyze
   */
  void init(AnomalyFunctionDTO spec) throws Exception;

  /**
   * Returns the specification for this function instance.
   */
  AnomalyFunctionDTO getSpec();

  /**
   * Returns the intervals of time series that is used by this anomaly function. This method is
   * useful when additional time series are needed for predicting the expected time series.
   *
   * @param monitoringWindowStartTime inclusive
   * @param monitoringWindowEndTime exclusive
   * @return intervals of time series that are used by this anomaly function
   */
  List<Interval> getTimeSeriesIntervals(long monitoringWindowStartTime,
      long monitoringWindowEndTime);

  /**
   * The anomaly detection is executed in the following flow:
   * 1. Transform current and baseline time series.
   * 2. Train prediction model using the baseline time series.
   * 3. Detect anomalies on the observed (current) time series against the expected time series,
   * which is computed by the prediction model.
   *
   * @return a list of raw anomalies
   */
  List<AnomalyResult> analyze(AnomalyDetectionContext anomalyDetectionContext) throws Exception;

  /**
   * Updates the information of the given merged anomaly.
   *
   * @param anomalyDetectionContext context that provide time series data
   * @param anomalyToUpdated the anomaly to be updated
   */
  void updateMergedAnomalyInfo(AnomalyDetectionContext anomalyDetectionContext,
      MergedAnomalyResultDTO anomalyToUpdated) throws Exception;

  /**
   * Returns the time series that are located in the given time window.
   *
   * @param anomalyDetectionContext context that provide time series data
   * @param bucketMillis bucket size in millis
   * @param metric the target metric name
   * @param viewWindowStartTime window start, inclusive
   * @param viewWindowEndTime window end, exclusive
   * @param knownAnomalies known anomalies
   * @return the time series that are located in the given time window.
   */
  AnomalyTimelinesView getTimeSeriesView(AnomalyDetectionContext anomalyDetectionContext,
      long bucketMillis,
      String metric, long viewWindowStartTime, long viewWindowEndTime,
      List<MergedAnomalyResultDTO> knownAnomalies);
}
