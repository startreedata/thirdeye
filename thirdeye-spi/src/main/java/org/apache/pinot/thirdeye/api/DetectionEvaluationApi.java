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
 *
 */

package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.List;
import org.apache.pinot.thirdeye.dataframe.DataFrame;

@JsonInclude(Include.NON_NULL)
public class DetectionEvaluationApi {

  private AlertComponentApi detection;
  private List<AnomalyApi> anomalies;
  private Date lastTimestamp;

  private DataFrame data;

  public AlertComponentApi getDetection() {
    return detection;
  }

  public DetectionEvaluationApi setDetection(
      final AlertComponentApi detection) {
    this.detection = detection;
    return this;
  }

  public List<AnomalyApi> getAnomalies() {
    return anomalies;
  }

  public DetectionEvaluationApi setAnomalies(
      final List<AnomalyApi> anomalies) {
    this.anomalies = anomalies;
    return this;
  }

  public Date getLastTimestamp() {
    return lastTimestamp;
  }

  public DetectionEvaluationApi setLastTimestamp(final Date lastTimestamp) {
    this.lastTimestamp = lastTimestamp;
    return this;
  }

  public DataFrame getData() {
    return data;
  }

  public DetectionEvaluationApi setData(final DataFrame data) {
    this.data = data;
    return this;
  }
}
