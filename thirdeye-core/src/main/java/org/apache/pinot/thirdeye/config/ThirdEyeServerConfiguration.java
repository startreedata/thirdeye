package org.apache.pinot.thirdeye.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.List;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.detection.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.events.MockEventsConfiguration;
import org.apache.pinot.thirdeye.metric.PrometheusConfiguration;
import org.apache.pinot.thirdeye.notification.commons.NotificationConfiguration;
import org.apache.pinot.thirdeye.restclient.ThirdEyeRestClientConfiguration;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;
import org.apache.pinot.thirdeye.scheduler.ThirdEyeSchedulerConfiguration;
import org.apache.pinot.thirdeye.task.TaskDriverConfiguration;

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
  private RCAConfiguration rcaConfiguration = new RCAConfiguration();

  @JsonProperty("notifications")
  private NotificationConfiguration notificationConfiguration = new NotificationConfiguration();

  @JsonProperty("ui")
  private UiConfiguration uiConfiguration = new UiConfiguration();

  @JsonProperty("prometheus")
  private PrometheusConfiguration prometheusConfiguration = new PrometheusConfiguration();

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

  public RCAConfiguration getRcaConfiguration() {
    return rcaConfiguration;
  }

  public ThirdEyeServerConfiguration setRcaConfiguration(
      final RCAConfiguration rcaConfiguration) {
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
}
