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
package ai.startree.thirdeye.config;

import ai.startree.thirdeye.auth.AccessControlConfiguration;
import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineConfiguration;
import ai.startree.thirdeye.notification.NotificationConfiguration;
import ai.startree.thirdeye.rootcause.configuration.RcaConfiguration;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerConfiguration;
import ai.startree.thirdeye.scheduler.events.MockEventsConfiguration;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdEyeServerConfiguration extends Configuration {

  @JsonProperty("auth")
  private AuthConfiguration authConfiguration;

  @JsonProperty("database")
  private DatabaseConfiguration databaseConfiguration;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty("mockEvents")
  private MockEventsConfiguration mockEventsConfiguration = new MockEventsConfiguration();

  @JsonProperty("taskDriver")
  private TaskDriverConfiguration taskDriverConfiguration = new TaskDriverConfiguration();

  @JsonProperty("scheduler")
  private ThirdEyeSchedulerConfiguration schedulerConfiguration = new ThirdEyeSchedulerConfiguration();

  @JsonProperty("detectionPipeline")
  private DetectionPipelineConfiguration detectionPipelineConfiguration = new DetectionPipelineConfiguration();

  @JsonProperty("rca")
  private RcaConfiguration rcaConfiguration = new RcaConfiguration();

  @JsonProperty("notifications")
  private NotificationConfiguration notificationConfiguration = new NotificationConfiguration();

  @JsonProperty("ui")
  private UiConfiguration uiConfiguration = new UiConfiguration();

  @JsonProperty("prometheus")
  private PrometheusConfiguration prometheusConfiguration = new PrometheusConfiguration();
  
  @JsonProperty("sentry")
  private BackendSentryConfiguration sentryConfiguration = new BackendSentryConfiguration();

  @JsonProperty("time")
  private TimeConfiguration timeConfiguration = new TimeConfiguration();

  @JsonProperty("accessControl")
  private AccessControlConfiguration accessControlConfiguration = new AccessControlConfiguration();

  private String phantomJsPath = "";
  private String failureFromAddress;
  private String failureToAddress;
  private List<String> holidayCountriesWhitelist;
  private String rootDir = "";

  public AuthConfiguration getAuthConfiguration() {
    return authConfiguration;
  }

  public ThirdEyeServerConfiguration setAuthConfiguration(
      final AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
    return this;
  }

  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  public void setSwaggerBundleConfiguration(
      final SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public ThirdEyeServerConfiguration setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }

  public MockEventsConfiguration getMockEventsConfiguration() {
    return mockEventsConfiguration;
  }

  public ThirdEyeServerConfiguration setMockEventsConfiguration(
      final MockEventsConfiguration mockEventsConfiguration) {
    this.mockEventsConfiguration = mockEventsConfiguration;
    return this;
  }

  public TaskDriverConfiguration getTaskDriverConfiguration() {
    return taskDriverConfiguration;
  }

  public ThirdEyeServerConfiguration setTaskDriverConfiguration(
      final TaskDriverConfiguration taskDriverConfiguration) {
    this.taskDriverConfiguration = taskDriverConfiguration;
    return this;
  }

  public NotificationConfiguration getNotificationConfiguration() {
    return notificationConfiguration;
  }

  public ThirdEyeServerConfiguration setNotificationConfiguration(
      final NotificationConfiguration notificationConfiguration) {
    this.notificationConfiguration = notificationConfiguration;
    return this;
  }

  public String getPhantomJsPath() {
    return phantomJsPath;
  }

  public ThirdEyeServerConfiguration setPhantomJsPath(final String phantomJsPath) {
    this.phantomJsPath = phantomJsPath;
    return this;
  }

  public String getFailureFromAddress() {
    return failureFromAddress;
  }

  public ThirdEyeServerConfiguration setFailureFromAddress(final String failureFromAddress) {
    this.failureFromAddress = failureFromAddress;
    return this;
  }

  public String getFailureToAddress() {
    return failureToAddress;
  }

  public ThirdEyeServerConfiguration setFailureToAddress(final String failureToAddress) {
    this.failureToAddress = failureToAddress;
    return this;
  }

  public List<String> getHolidayCountriesWhitelist() {
    return holidayCountriesWhitelist;
  }

  public ThirdEyeServerConfiguration setHolidayCountriesWhitelist(
      final List<String> holidayCountriesWhitelist) {
    this.holidayCountriesWhitelist = holidayCountriesWhitelist;
    return this;
  }

  public String getRootDir() {
    return rootDir;
  }

  public ThirdEyeServerConfiguration setRootDir(final String rootDir) {
    this.rootDir = rootDir;
    return this;
  }

  public ThirdEyeSchedulerConfiguration getSchedulerConfiguration() {
    return schedulerConfiguration;
  }

  public ThirdEyeServerConfiguration setSchedulerConfiguration(
      final ThirdEyeSchedulerConfiguration schedulerConfiguration) {
    this.schedulerConfiguration = schedulerConfiguration;
    return this;
  }

  public RcaConfiguration getRcaConfiguration() {
    return rcaConfiguration;
  }

  public ThirdEyeServerConfiguration setRcaConfiguration(
      final RcaConfiguration rcaConfiguration) {
    this.rcaConfiguration = rcaConfiguration;
    return this;
  }

  public UiConfiguration getUiConfiguration() {
    return uiConfiguration;
  }

  public ThirdEyeServerConfiguration setUiConfiguration(
      final UiConfiguration uiConfiguration) {
    this.uiConfiguration = uiConfiguration;
    return this;
  }

  public PrometheusConfiguration getPrometheusConfiguration() {
    return prometheusConfiguration;
  }

  public ThirdEyeServerConfiguration setPrometheusConfiguration(
      final PrometheusConfiguration prometheusConfiguration) {
    this.prometheusConfiguration = prometheusConfiguration;
    return this;
  }

  public TimeConfiguration getTimeConfiguration() {
    return timeConfiguration;
  }

  public ThirdEyeServerConfiguration setTimeConfiguration(
      final TimeConfiguration timeConfiguration) {
    this.timeConfiguration = timeConfiguration;
    return this;
  }

  public DetectionPipelineConfiguration getDetectionPipelineConfiguration() {
    return detectionPipelineConfiguration;
  }

  public ThirdEyeServerConfiguration setDetectionPipelineConfiguration(
      final DetectionPipelineConfiguration detectionPipelineConfiguration) {
    this.detectionPipelineConfiguration = detectionPipelineConfiguration;
    return this;
  }

  public AccessControlConfiguration getAccessControlConfiguration() {
    return this.accessControlConfiguration;
  }

  public ThirdEyeServerConfiguration setAccessControlConfiguration(AccessControlConfiguration config) {
    this.accessControlConfiguration = config;
    return this;
  }

  public BackendSentryConfiguration getSentryConfiguration() {
    return sentryConfiguration;
  }

  public ThirdEyeServerConfiguration setSentryConfiguration(
      final BackendSentryConfiguration sentryConfiguration) {
    this.sentryConfiguration = sentryConfiguration;
    return this;
  }
}
