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

public class ThirdEyeCoordinatorConfiguration extends Configuration {

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

  public ThirdEyeCoordinatorConfiguration setAuthConfiguration(
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

  public ThirdEyeCoordinatorConfiguration setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }

  public String getConfigPath() {
    return configPath;
  }

  public ThirdEyeCoordinatorConfiguration setConfigPath(final String configPath) {
    this.configPath = configPath;
    return this;
  }

  public MockEventsConfiguration getMockEventsConfiguration() {
    return mockEventsConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setMockEventsConfiguration(
      final MockEventsConfiguration mockEventsConfiguration) {
    this.mockEventsConfiguration = mockEventsConfiguration;
    return this;
  }

  public TaskDriverConfiguration getTaskDriverConfiguration() {
    return taskDriverConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setTaskDriverConfiguration(
      final TaskDriverConfiguration taskDriverConfiguration) {
    this.taskDriverConfiguration = taskDriverConfiguration;
    return this;
  }

  public NotificationConfiguration getAlerterConfigurations() {
    return notificationConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setAlerterConfigurations(
      final NotificationConfiguration notificationConfiguration) {
    this.notificationConfiguration = notificationConfiguration;
    return this;
  }

  public String getPhantomJsPath() {
    return phantomJsPath;
  }

  public ThirdEyeCoordinatorConfiguration setPhantomJsPath(final String phantomJsPath) {
    this.phantomJsPath = phantomJsPath;
    return this;
  }

  public String getFailureFromAddress() {
    return failureFromAddress;
  }

  public ThirdEyeCoordinatorConfiguration setFailureFromAddress(final String failureFromAddress) {
    this.failureFromAddress = failureFromAddress;
    return this;
  }

  public String getFailureToAddress() {
    return failureToAddress;
  }

  public ThirdEyeCoordinatorConfiguration setFailureToAddress(final String failureToAddress) {
    this.failureToAddress = failureToAddress;
    return this;
  }

  public ThirdEyeRestClientConfiguration getTeRestConfig() {
    return teRestConfig;
  }

  public ThirdEyeCoordinatorConfiguration setTeRestConfig(
      final ThirdEyeRestClientConfiguration teRestConfig) {
    this.teRestConfig = teRestConfig;
    return this;
  }

  public DataAvailabilitySchedulingConfiguration getDataAvailabilitySchedulingConfiguration() {
    return dataAvailabilitySchedulingConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setDataAvailabilitySchedulingConfiguration(
      final DataAvailabilitySchedulingConfiguration dataAvailabilitySchedulingConfiguration) {
    this.dataAvailabilitySchedulingConfiguration = dataAvailabilitySchedulingConfiguration;
    return this;
  }

  public List<String> getHolidayCountriesWhitelist() {
    return holidayCountriesWhitelist;
  }

  public ThirdEyeCoordinatorConfiguration setHolidayCountriesWhitelist(
      final List<String> holidayCountriesWhitelist) {
    this.holidayCountriesWhitelist = holidayCountriesWhitelist;
    return this;
  }

  public String getRootDir() {
    return rootDir;
  }

  public ThirdEyeCoordinatorConfiguration setRootDir(final String rootDir) {
    this.rootDir = rootDir;
    return this;
  }

  public ThirdEyeSchedulerConfiguration getSchedulerConfiguration() {
    return schedulerConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setSchedulerConfiguration(
      final ThirdEyeSchedulerConfiguration schedulerConfiguration) {
    this.schedulerConfiguration = schedulerConfiguration;
    return this;
  }

  public CacheConfig getCacheConfig() {
    return cacheConfig;
  }

  public ThirdEyeCoordinatorConfiguration setCacheConfig(
      final CacheConfig cacheConfig) {
    this.cacheConfig = cacheConfig;
    return this;
  }

  public RCAConfiguration getRcaConfiguration() {
    return rcaConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setRcaConfiguration(
      final RCAConfiguration rcaConfiguration) {
    this.rcaConfiguration = rcaConfiguration;
    return this;
  }

  public UiConfiguration getUiConfiguration() {
    return uiConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setUiConfiguration(
      final UiConfiguration uiConfiguration) {
    this.uiConfiguration = uiConfiguration;
    return this;
  }

  public PrometheusConfiguration getPrometheusConfiguration() {
    return prometheusConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setPrometheusConfiguration(
      final PrometheusConfiguration prometheusConfiguration) {
    this.prometheusConfiguration = prometheusConfiguration;
    return this;
  }
}
