package org.apache.pinot.thirdeye.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.DataAvailabilityEventListenerDriver;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.DataAvailabilityTaskScheduler;
import org.apache.pinot.thirdeye.anomaly.events.HolidayEventsLoader;
import org.apache.pinot.thirdeye.anomaly.events.MockEventsLoader;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorJobScheduler;
import org.apache.pinot.thirdeye.auto.onboard.AutoOnboardService;
import org.apache.pinot.thirdeye.config.HolidayEventsLoaderConfiguration;
import org.apache.pinot.thirdeye.model.download.ModelDownloaderManager;

@Singleton
public class SchedulerService implements Managed {

  private final ThirdEyeWorkerConfiguration config;
  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final MonitorJobScheduler monitorJobScheduler;
  private final AutoOnboardService autoOnboardService;
  private final HolidayEventsLoader holidayEventsLoader;
  private final DetectionCronScheduler detectionScheduler;
  private final DataAvailabilityEventListenerDriver dataAvailabilityEventListenerDriver;
  private final ModelDownloaderManager modelDownloaderManager;
  private final DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler;
  private final SubscriptionCronScheduler subscriptionScheduler;
  private final MockEventsLoader mockEventsLoader;

  @Inject
  public SchedulerService(final ThirdEyeWorkerConfiguration config,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final MonitorJobScheduler monitorJobScheduler,
      final AutoOnboardService autoOnboardService,
      final HolidayEventsLoader holidayEventsLoader,
      final DetectionCronScheduler detectionScheduler,
      final DataAvailabilityEventListenerDriver dataAvailabilityEventListenerDriver,
      final ModelDownloaderManager modelDownloaderManager,
      final DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler,
      final SubscriptionCronScheduler subscriptionScheduler,
      final MockEventsLoader mockEventsLoader) {
    this.config = config;
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.monitorJobScheduler = monitorJobScheduler;
    this.autoOnboardService = autoOnboardService;
    this.holidayEventsLoader = holidayEventsLoader;
    this.detectionScheduler = detectionScheduler;
    this.dataAvailabilityEventListenerDriver = dataAvailabilityEventListenerDriver;
    this.modelDownloaderManager = modelDownloaderManager;
    this.dataAvailabilityTaskScheduler = dataAvailabilityTaskScheduler;
    this.subscriptionScheduler = subscriptionScheduler;
    this.mockEventsLoader = mockEventsLoader;
  }

  @Override
  public void start() throws Exception {
    if (config.isMonitor()) {
      monitorJobScheduler.start();
    }

    if (config.isAutoload()) {
      autoOnboardService.start();
    }

    if (holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.start();
    }
    if (config.isMockEventsLoader()) {
      mockEventsLoader.run();
    }
    if (config.isDetectionPipeline()) {
      detectionScheduler.start();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.start();
    }
    if (config.isDataAvailabilityEventListener()) {
      dataAvailabilityEventListenerDriver.start();
    }
    if (config.isDataAvailabilityTaskScheduler()) {
      dataAvailabilityTaskScheduler.start();
    }
    if (config.getModelDownloaderConfig() != null) {
      modelDownloaderManager.start();
    }
  }

  @Override
  public void stop() throws Exception {
    if (monitorJobScheduler != null) {
      monitorJobScheduler.shutdown();
    }
    if (holidayEventsLoader != null && holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.shutdown();
    }
    if (autoOnboardService != null) {
      autoOnboardService.shutdown();
    }
    if (detectionScheduler != null) {
      detectionScheduler.shutdown();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.shutdown();
    }
    if (dataAvailabilityEventListenerDriver != null) {
      dataAvailabilityEventListenerDriver.shutdown();
    }
    if (config.isDataAvailabilityTaskScheduler()) {
      dataAvailabilityTaskScheduler.shutdown();
    }
    if (modelDownloaderManager != null) {
      modelDownloaderManager.shutdown();
    }
  }
}
