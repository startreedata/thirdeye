/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.tracking;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestStatisticsLogger implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(RequestStatisticsLogger.class);
  private static final RequestLog requestLog = new RequestLog(1000000);

  private final ScheduledExecutorService scheduledExecutorService;
  private final TimeGranularity runFrequency;

  public RequestStatisticsLogger(TimeGranularity runFrequency) {
    this.runFrequency = runFrequency;
    this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
  }

  public static RequestLog getRequestLog() {
    return requestLog;
  }

  @Override
  public void run() {
    try {
      long timestamp = System.nanoTime();
      RequestStatistics stats = getRequestLog().getStatistics(timestamp);
      getRequestLog().truncate(timestamp);

      RequestStatisticsFormatter formatter = new RequestStatisticsFormatter();
      LOG.info("Recent request performance statistics:\n{}", formatter.format(stats));
    } catch (Exception e) {
      LOG.error("Could not generate statistics", e);
    }
  }

  public void start() {
    LOG.info("starting logger");
    this.scheduledExecutorService.scheduleWithFixedDelay(this,
        this.runFrequency.getSize(), this.runFrequency.getSize(), this.runFrequency.getUnit());
  }

  public void shutdown() {
    LOG.info("stopping logger");
    this.scheduledExecutorService.shutdown();
  }
}
