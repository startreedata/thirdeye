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

package org.apache.pinot.thirdeye.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.List;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.anomaly.task.TaskDriverConfiguration;
import org.apache.pinot.thirdeye.common.restclient.ThirdEyeRestClientConfiguration;

public class ThirdEyeWorkerConfiguration extends ThirdEyeConfiguration {

  private boolean scheduler = false;

  @JsonProperty("taskDriver")
  private TaskDriverConfiguration taskDriverConfiguration = new TaskDriverConfiguration();

  private ThirdEyeRestClientConfiguration teRestConfig = new ThirdEyeRestClientConfiguration();
  private DataAvailabilitySchedulingConfiguration
      dataAvailabilitySchedulingConfiguration = new DataAvailabilitySchedulingConfiguration();
  private List<String> holidayCountriesWhitelist;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfiguration;

  public boolean isScheduler() {
    return scheduler;
  }

  public ThirdEyeWorkerConfiguration setScheduler(final boolean scheduler) {
    this.scheduler = scheduler;
    return this;
  }

  public TaskDriverConfiguration getTaskDriverConfiguration() {
    return taskDriverConfiguration;
  }

  public ThirdEyeWorkerConfiguration setTaskDriverConfiguration(
      final TaskDriverConfiguration taskDriverConfiguration) {
    this.taskDriverConfiguration = taskDriverConfiguration;
    return this;
  }

  public ThirdEyeRestClientConfiguration getTeRestConfig() {
    return teRestConfig;
  }

  public ThirdEyeWorkerConfiguration setTeRestConfig(
      final ThirdEyeRestClientConfiguration teRestConfig) {
    this.teRestConfig = teRestConfig;
    return this;
  }

  public DataAvailabilitySchedulingConfiguration getDataAvailabilitySchedulingConfiguration() {
    return dataAvailabilitySchedulingConfiguration;
  }

  public ThirdEyeWorkerConfiguration setDataAvailabilitySchedulingConfiguration(
      final DataAvailabilitySchedulingConfiguration dataAvailabilitySchedulingConfiguration) {
    this.dataAvailabilitySchedulingConfiguration = dataAvailabilitySchedulingConfiguration;
    return this;
  }

  public List<String> getHolidayCountriesWhitelist() {
    return holidayCountriesWhitelist;
  }

  public ThirdEyeWorkerConfiguration setHolidayCountriesWhitelist(
      final List<String> holidayCountriesWhitelist) {
    this.holidayCountriesWhitelist = holidayCountriesWhitelist;
    return this;
  }

  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  public ThirdEyeWorkerConfiguration setSwaggerBundleConfiguration(
      final SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
    return this;
  }
}
