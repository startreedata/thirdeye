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

package org.apache.pinot.thirdeye.anomaly;

import java.util.List;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorConfiguration;
import org.apache.pinot.thirdeye.anomaly.task.TaskDriverConfiguration;
import org.apache.pinot.thirdeye.auto.onboard.AutoOnboardConfiguration;
import org.apache.pinot.thirdeye.common.ThirdEyeConfiguration;
import org.apache.pinot.thirdeye.common.restclient.ThirdEyeRestClientConfiguration;

public class ThirdEyeWorkerConfiguration extends ThirdEyeConfiguration {

  private boolean alert = false;
  private boolean autoload = false;
  private boolean holidayEventsLoader = false;
  private boolean mockEventsLoader = false;
  private boolean monitor = false;
  private boolean pinotProxy = false;
  private boolean scheduler = false;
  private boolean worker = false;
  private boolean onlineWorker = false;
  private boolean detectionPipeline = false;
  private boolean detectionAlert = false;
  private boolean dataAvailabilityEventListener = false;
  private boolean dataAvailabilityTaskScheduler = false;

  private long id;
  private HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration = new HolidayEventsLoaderConfiguration();
  private MockEventsLoaderConfiguration mockEventsLoaderConfiguration = new MockEventsLoaderConfiguration();
  private MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
  private AutoOnboardConfiguration autoOnboardConfiguration = new AutoOnboardConfiguration();
  private TaskDriverConfiguration taskDriverConfiguration = new TaskDriverConfiguration();
  private ThirdEyeRestClientConfiguration teRestConfig = new ThirdEyeRestClientConfiguration();
  private DataAvailabilitySchedulingConfiguration
      dataAvailabilitySchedulingConfiguration = new DataAvailabilitySchedulingConfiguration();
  private List<String> holidayCountriesWhitelist;

  public boolean isAlert() {
    return alert;
  }

  public ThirdEyeWorkerConfiguration setAlert(final boolean alert) {
    this.alert = alert;
    return this;
  }

  public boolean isAutoload() {
    return autoload;
  }

  public ThirdEyeWorkerConfiguration setAutoload(final boolean autoload) {
    this.autoload = autoload;
    return this;
  }

  public boolean isHolidayEventsLoader() {
    return holidayEventsLoader;
  }

  public ThirdEyeWorkerConfiguration setHolidayEventsLoader(final boolean holidayEventsLoader) {
    this.holidayEventsLoader = holidayEventsLoader;
    return this;
  }

  public boolean isMockEventsLoader() {
    return mockEventsLoader;
  }

  public ThirdEyeWorkerConfiguration setMockEventsLoader(final boolean mockEventsLoader) {
    this.mockEventsLoader = mockEventsLoader;
    return this;
  }

  public boolean isMonitor() {
    return monitor;
  }

  public ThirdEyeWorkerConfiguration setMonitor(final boolean monitor) {
    this.monitor = monitor;
    return this;
  }

  public boolean isPinotProxy() {
    return pinotProxy;
  }

  public ThirdEyeWorkerConfiguration setPinotProxy(final boolean pinotProxy) {
    this.pinotProxy = pinotProxy;
    return this;
  }

  public boolean isScheduler() {
    return scheduler;
  }

  public ThirdEyeWorkerConfiguration setScheduler(final boolean scheduler) {
    this.scheduler = scheduler;
    return this;
  }

  public boolean isWorker() {
    return worker;
  }

  public ThirdEyeWorkerConfiguration setWorker(final boolean worker) {
    this.worker = worker;
    return this;
  }

  public boolean isOnlineWorker() {
    return onlineWorker;
  }

  public ThirdEyeWorkerConfiguration setOnlineWorker(final boolean onlineWorker) {
    this.onlineWorker = onlineWorker;
    return this;
  }

  public boolean isDetectionPipeline() {
    return detectionPipeline;
  }

  public ThirdEyeWorkerConfiguration setDetectionPipeline(final boolean detectionPipeline) {
    this.detectionPipeline = detectionPipeline;
    return this;
  }

  public boolean isDetectionAlert() {
    return detectionAlert;
  }

  public ThirdEyeWorkerConfiguration setDetectionAlert(final boolean detectionAlert) {
    this.detectionAlert = detectionAlert;
    return this;
  }

  public boolean isDataAvailabilityEventListener() {
    return dataAvailabilityEventListener;
  }

  public ThirdEyeWorkerConfiguration setDataAvailabilityEventListener(
      final boolean dataAvailabilityEventListener) {
    this.dataAvailabilityEventListener = dataAvailabilityEventListener;
    return this;
  }

  public boolean isDataAvailabilityTaskScheduler() {
    return dataAvailabilityTaskScheduler;
  }

  public ThirdEyeWorkerConfiguration setDataAvailabilityTaskScheduler(
      final boolean dataAvailabilityTaskScheduler) {
    this.dataAvailabilityTaskScheduler = dataAvailabilityTaskScheduler;
    return this;
  }

  public long getId() {
    return id;
  }

  public ThirdEyeWorkerConfiguration setId(final long id) {
    this.id = id;
    return this;
  }

  public HolidayEventsLoaderConfiguration getHolidayEventsLoaderConfiguration() {
    return holidayEventsLoaderConfiguration;
  }

  public ThirdEyeWorkerConfiguration setHolidayEventsLoaderConfiguration(
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration) {
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    return this;
  }

  public MockEventsLoaderConfiguration getMockEventsLoaderConfiguration() {
    return mockEventsLoaderConfiguration;
  }

  public ThirdEyeWorkerConfiguration setMockEventsLoaderConfiguration(
      final MockEventsLoaderConfiguration mockEventsLoaderConfiguration) {
    this.mockEventsLoaderConfiguration = mockEventsLoaderConfiguration;
    return this;
  }

  public MonitorConfiguration getMonitorConfiguration() {
    return monitorConfiguration;
  }

  public ThirdEyeWorkerConfiguration setMonitorConfiguration(
      final MonitorConfiguration monitorConfiguration) {
    this.monitorConfiguration = monitorConfiguration;
    return this;
  }

  public AutoOnboardConfiguration getAutoOnboardConfiguration() {
    return autoOnboardConfiguration;
  }

  public ThirdEyeWorkerConfiguration setAutoOnboardConfiguration(
      final AutoOnboardConfiguration autoOnboardConfiguration) {
    this.autoOnboardConfiguration = autoOnboardConfiguration;
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
}
