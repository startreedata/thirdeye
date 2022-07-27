/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.scheduler;

import ai.startree.thirdeye.scheduler.autoonboard.AutoOnboardConfiguration;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.scheduler.modeldownload.ModelDownloaderConfiguration;
import ai.startree.thirdeye.scheduler.monitor.MonitorConfiguration;
import ai.startree.thirdeye.scheduler.monitor.TaskCleanUpConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ThirdEyeSchedulerConfiguration {

  private boolean enabled = false;
  private boolean monitor = false;
  private boolean detectionPipeline = false;
  private boolean detectionAlert = false;
  private boolean dataAvailabilityEventListener = false;
  private boolean dataAvailabilityTaskScheduler = false;

  @JsonProperty("taskCleanUp")
  private TaskCleanUpConfiguration taskCleanUpConfiguration = new TaskCleanUpConfiguration();

  @JsonProperty("holidayEvents")
  private HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration = new HolidayEventsLoaderConfiguration();

  @JsonProperty("autoOnboard")
  private AutoOnboardConfiguration autoOnboardConfiguration = new AutoOnboardConfiguration();

  private MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
  private List<ModelDownloaderConfiguration> modelDownloaderConfigs;

  public boolean isEnabled() {
    return enabled;
  }

  public TaskCleanUpConfiguration getTaskCleanUpConfiguration() {
    return taskCleanUpConfiguration;
  }

  public ThirdEyeSchedulerConfiguration setTaskCleanUpConfiguration(
      final TaskCleanUpConfiguration taskCleanUpConfiguration) {
    this.taskCleanUpConfiguration = taskCleanUpConfiguration;
    return this;
  }

  public ThirdEyeSchedulerConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public HolidayEventsLoaderConfiguration getHolidayEventsLoaderConfiguration() {
    return holidayEventsLoaderConfiguration;
  }

  public ThirdEyeSchedulerConfiguration setHolidayEventsLoaderConfiguration(
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration) {
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    return this;
  }

  public boolean isMonitor() {
    return monitor;
  }

  public ThirdEyeSchedulerConfiguration setMonitor(final boolean monitor) {
    this.monitor = monitor;
    return this;
  }

  public boolean isDetectionPipeline() {
    return detectionPipeline;
  }

  public ThirdEyeSchedulerConfiguration setDetectionPipeline(final boolean detectionPipeline) {
    this.detectionPipeline = detectionPipeline;
    return this;
  }

  public boolean isDetectionAlert() {
    return detectionAlert;
  }

  public ThirdEyeSchedulerConfiguration setDetectionAlert(final boolean detectionAlert) {
    this.detectionAlert = detectionAlert;
    return this;
  }

  public boolean isDataAvailabilityEventListener() {
    return dataAvailabilityEventListener;
  }

  public ThirdEyeSchedulerConfiguration setDataAvailabilityEventListener(
      final boolean dataAvailabilityEventListener) {
    this.dataAvailabilityEventListener = dataAvailabilityEventListener;
    return this;
  }

  public boolean isDataAvailabilityTaskScheduler() {
    return dataAvailabilityTaskScheduler;
  }

  public ThirdEyeSchedulerConfiguration setDataAvailabilityTaskScheduler(
      final boolean dataAvailabilityTaskScheduler) {
    this.dataAvailabilityTaskScheduler = dataAvailabilityTaskScheduler;
    return this;
  }

  public MonitorConfiguration getMonitorConfiguration() {
    return monitorConfiguration;
  }

  public ThirdEyeSchedulerConfiguration setMonitorConfiguration(
      final MonitorConfiguration monitorConfiguration) {
    this.monitorConfiguration = monitorConfiguration;
    return this;
  }

  public AutoOnboardConfiguration getAutoOnboardConfiguration() {
    return autoOnboardConfiguration;
  }

  public ThirdEyeSchedulerConfiguration setAutoOnboardConfiguration(
      final AutoOnboardConfiguration autoOnboardConfiguration) {
    this.autoOnboardConfiguration = autoOnboardConfiguration;
    return this;
  }

  public List<ModelDownloaderConfiguration> getModelDownloaderConfigs() {
    return modelDownloaderConfigs;
  }

  public ThirdEyeSchedulerConfiguration setModelDownloaderConfigs(
      final List<ModelDownloaderConfiguration> modelDownloaderConfigs) {
    this.modelDownloaderConfigs = modelDownloaderConfigs;
    return this;
  }
}
