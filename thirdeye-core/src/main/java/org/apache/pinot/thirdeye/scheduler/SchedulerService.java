package org.apache.pinot.thirdeye.scheduler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeAnomalyConfiguration;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.DataAvailabilityEventListenerDriver;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.DataAvailabilityTaskScheduler;
import org.apache.pinot.thirdeye.anomaly.events.HolidayEventsLoader;
import org.apache.pinot.thirdeye.anomaly.events.MockEventsLoader;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorJobScheduler;
import org.apache.pinot.thirdeye.auto.onboard.AutoOnboardService;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.model.download.ModelDownloaderManager;

@Singleton
public class SchedulerService implements Managed {

  private final ThirdEyeAnomalyConfiguration config;
  private MonitorJobScheduler monitorJobScheduler = null;
  private DetectionCronScheduler detectionScheduler = null;
  private AutoOnboardService autoOnboardService = null;
  private HolidayEventsLoader holidayEventsLoader = null;
  private DataAvailabilityEventListenerDriver dataAvailabilityEventListenerDriver = null;
  private ModelDownloaderManager modelDownloaderManager = null;

  @Inject
  public SchedulerService(final ThirdEyeAnomalyConfiguration config) {
    this.config = config;
  }

  @Override
  public void start() throws Exception {

    if (config.isMonitor()) {
      monitorJobScheduler = new MonitorJobScheduler(config.getMonitorConfiguration());
      monitorJobScheduler.start();
    }
    if (config.isAutoload()) {
      autoOnboardService = new AutoOnboardService(config);
      autoOnboardService.start();
    }
    if (config.isHolidayEventsLoader()) {
      holidayEventsLoader =
          new HolidayEventsLoader(config.getHolidayEventsLoaderConfiguration(),
              config.getCalendarApiKeyPath(),
              DAORegistry.getInstance().getEventDAO());
      holidayEventsLoader.start();
      // TODO suvodeep Move this to Coordinator
      //      env.jersey().register(new HolidayEventResource(holidayEventsLoader));
    }
    if (config.isMockEventsLoader()) {
      final MockEventsLoader mockEventsLoader = new MockEventsLoader(
          config.getMockEventsLoaderConfiguration(),
          DAORegistry.getInstance().getEventDAO());
      mockEventsLoader.run();
    }
    if (config.isDetectionPipeline()) {
      detectionScheduler = new DetectionCronScheduler(
          DAORegistry.getInstance().getDetectionConfigManager());
      detectionScheduler.start();
    }
    if (config.isDetectionAlert()) {
      final SubscriptionCronScheduler subscriptionScheduler = new SubscriptionCronScheduler();
      subscriptionScheduler.start();
    }
    if (config.isDataAvailabilityEventListener()) {
      dataAvailabilityEventListenerDriver = new DataAvailabilityEventListenerDriver(
          config.getDataAvailabilitySchedulingConfiguration());
      dataAvailabilityEventListenerDriver.start();
    }
    if (config.isDataAvailabilityTaskScheduler()) {
      final DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler = new DataAvailabilityTaskScheduler(
          config.getDataAvailabilitySchedulingConfiguration().getSchedulerDelayInSec(),
          config.getDataAvailabilitySchedulingConfiguration().getTaskTriggerFallBackTimeInSec(),
          config.getDataAvailabilitySchedulingConfiguration().getSchedulingWindowInSec(),
          config.getDataAvailabilitySchedulingConfiguration().getScheduleDelayInSec());
      dataAvailabilityTaskScheduler.start();
    }
    if (config.getModelDownloaderConfig() != null) {
      modelDownloaderManager = new ModelDownloaderManager(config.getModelDownloaderConfig());
      modelDownloaderManager.start();
    }
  }

  @Override
  public void stop() throws Exception {
    if (monitorJobScheduler != null) {
      monitorJobScheduler.shutdown();
    }
    if (holidayEventsLoader != null) {
      holidayEventsLoader.shutdown();
    }
    if (autoOnboardService != null) {
      autoOnboardService.shutdown();
    }
    if (detectionScheduler != null) {
      detectionScheduler.shutdown();
    }
    if (dataAvailabilityEventListenerDriver != null) {
      dataAvailabilityEventListenerDriver.shutdown();
    }
    if (modelDownloaderManager != null) {
      modelDownloaderManager.shutdown();
    }
  }
}
