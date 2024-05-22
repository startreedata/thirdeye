/*
 * Copyright 2024 StarTree Inc
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

import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.scheduler.taskcleanup.TaskCleanUpConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class ThirdEyeSchedulerConfiguration {

  private boolean enabled = false;
  // TODO CYRIL not used anymore - can be removed around 09/2024
  private boolean monitor = false;
  private boolean detectionPipeline = false;
  private boolean detectionAlert = false;
  private boolean dataAvailabilityEventListener = false;
  private int alertUpdateDelay = 60;

  // TODO spyne: consolidate all the update delays into a single configuration after consolidating the core scheduler code
  private int subscriptionGroupUpdateDelay = 60;

  @JsonProperty("taskCleanUp")
  private TaskCleanUpConfiguration taskCleanUpConfiguration = new TaskCleanUpConfiguration();

  @JsonProperty("holidayEvents")
  private HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration = new HolidayEventsLoaderConfiguration();

  // TODO CYRIL not used anymore - can be removed 
  private Map<String, Object> monitorConfiguration = new HashMap<>();

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

  public int getAlertUpdateDelay() {
    return alertUpdateDelay;
  }

  public ThirdEyeSchedulerConfiguration setAlertUpdateDelay(final int alertUpdateDelay) {
    this.alertUpdateDelay = alertUpdateDelay;
    return this;
  }

  public int getSubscriptionGroupUpdateDelay() {
    return subscriptionGroupUpdateDelay;
  }

  public ThirdEyeSchedulerConfiguration setSubscriptionGroupUpdateDelay(
      final int subscriptionGroupUpdateDelay) {
    this.subscriptionGroupUpdateDelay = subscriptionGroupUpdateDelay;
    return this;
  }

  public Map<String, Object> getMonitorConfiguration() {
    return monitorConfiguration;
  }

  public ThirdEyeSchedulerConfiguration setMonitorConfiguration(
      final Map<String, Object> monitorConfiguration) {
    this.monitorConfiguration = monitorConfiguration;
    return this;
  }
}
