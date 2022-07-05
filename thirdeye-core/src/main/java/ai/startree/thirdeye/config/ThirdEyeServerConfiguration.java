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
package ai.startree.thirdeye.config;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.events.MockEventsConfiguration;
import ai.startree.thirdeye.metric.PrometheusConfiguration;
import ai.startree.thirdeye.notification.NotificationConfiguration;
import ai.startree.thirdeye.restclient.ThirdEyeRestClientConfiguration;
import ai.startree.thirdeye.rootcause.configuration.RcaConfiguration;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerConfiguration;
import ai.startree.thirdeye.task.TaskDriverConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.List;

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

  @JsonProperty("cache")
  private CacheConfig cacheConfig = new CacheConfig();

  @JsonProperty("rca")
  private RcaConfiguration rcaConfiguration = new RcaConfiguration();

  @JsonProperty("notifications")
  private NotificationConfiguration notificationConfiguration = new NotificationConfiguration();

  @JsonProperty("ui")
  private UiConfiguration uiConfiguration = new UiConfiguration();

  @JsonProperty("prometheus")
  private PrometheusConfiguration prometheusConfiguration = new PrometheusConfiguration();

  @JsonProperty("time")
  private TimeConfiguration timeConfiguration = new TimeConfiguration();

  private String configPath = "config";

  private String phantomJsPath = "";
  private String failureFromAddress;
  private String failureToAddress;
  private ThirdEyeRestClientConfiguration teRestConfig = new ThirdEyeRestClientConfiguration();
  private DataAvailabilitySchedulingConfiguration
      dataAvailabilitySchedulingConfiguration = new DataAvailabilitySchedulingConfiguration();
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

  public String getConfigPath() {
    return configPath;
  }

  public ThirdEyeServerConfiguration setConfigPath(final String configPath) {
    this.configPath = configPath;
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

  public ThirdEyeRestClientConfiguration getTeRestConfig() {
    return teRestConfig;
  }

  public ThirdEyeServerConfiguration setTeRestConfig(
      final ThirdEyeRestClientConfiguration teRestConfig) {
    this.teRestConfig = teRestConfig;
    return this;
  }

  public DataAvailabilitySchedulingConfiguration getDataAvailabilitySchedulingConfiguration() {
    return dataAvailabilitySchedulingConfiguration;
  }

  public ThirdEyeServerConfiguration setDataAvailabilitySchedulingConfiguration(
      final DataAvailabilitySchedulingConfiguration dataAvailabilitySchedulingConfiguration) {
    this.dataAvailabilitySchedulingConfiguration = dataAvailabilitySchedulingConfiguration;
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

  public CacheConfig getCacheConfig() {
    return cacheConfig;
  }

  public ThirdEyeServerConfiguration setCacheConfig(
      final CacheConfig cacheConfig) {
    this.cacheConfig = cacheConfig;
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
}
