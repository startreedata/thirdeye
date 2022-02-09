package ai.startree.thirdeye.scheduler;

import ai.startree.thirdeye.datasource.AutoOnboardConfiguration;
import ai.startree.thirdeye.detection.anomaly.monitor.MonitorConfiguration;
import ai.startree.thirdeye.detection.download.ModelDownloaderConfiguration;
import ai.startree.thirdeye.events.HolidayEventsLoaderConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ThirdEyeSchedulerConfiguration {

  private boolean enabled = false;
  private boolean monitor = false;
  private boolean detectionPipeline = false;
  private boolean detectionAlert = false;
  private boolean dataAvailabilityEventListener = false;
  private boolean dataAvailabilityTaskScheduler = false;

  @JsonProperty("holidayEvents")
  private HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration = new HolidayEventsLoaderConfiguration();

  @JsonProperty("autoOnboard")
  private AutoOnboardConfiguration autoOnboardConfiguration = new AutoOnboardConfiguration();

  private MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
  private List<ModelDownloaderConfiguration> modelDownloaderConfigs;

  public boolean isEnabled() {
    return enabled;
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
