/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.scheduler.dataavailability;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.util.ThirdeyeMetricsUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is to listen to Kafka trigger events and update metadata in the metadata store based
 * on the events,
 * so that new anomaly detection can be trigger accordingly.
 */
public class DataAvailabilityEventListener implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(DataAvailabilityEventListener.class);
  private final DataAvailabilityKafkaConsumer consumer;
  private final List<DataAvailabilityEventFilter> filters;
  private final DatasetTriggerInfoRepo datasetTriggerInfoRepo;
  private final DatasetConfigManager datasetConfigManager;
  private final long sleepTimeInMilli;
  private final long pollTimeInMilli;

  public DataAvailabilityEventListener(DataAvailabilityKafkaConsumer consumer,
      List<DataAvailabilityEventFilter> filters,
      long sleepTimeInMilli,
      long pollTimeInMilli,
      final DatasetConfigManager datasetConfigManager,
      final DatasetTriggerInfoRepo datasetTriggerInfoRepo) {
    this.consumer = consumer;
    this.filters = filters;
    this.datasetConfigManager = datasetConfigManager;
    this.datasetTriggerInfoRepo = datasetTriggerInfoRepo;
    this.sleepTimeInMilli = sleepTimeInMilli;
    this.pollTimeInMilli = pollTimeInMilli;
  }

  @Override
  public void run() {
    try {
      while (!(Thread.interrupted())) {
        processOneBatch();
      }
    } catch (Exception e) {
      LOG.error("Caught an exception while processing event.", e);
    } finally {
      consumer.close();
    }
    LOG.info("DataAvailabilityEventListener under thread {} is closed.",
        Thread.currentThread().getName());
  }

  public void close() {
    datasetTriggerInfoRepo.close();
  }

  void processOneBatch() throws InterruptedException {
    List<DataAvailabilityEvent> events = consumer.poll(pollTimeInMilli);
    ThirdeyeMetricsUtil.triggerEventCounter.inc(events.size());
    for (DataAvailabilityEvent event : events) {
      if (checkAllFiltersPassed(event)) {
        try {
          LOG.info("Processing event: " + event.getDatasetName() + " with watermark " + event
              .getHighWatermark());
          String dataset = event.getDatasetName();
          datasetTriggerInfoRepo.setLastUpdateTimestamp(dataset, event.getHighWatermark());
          //Note: Batch update the timestamps of dataset if the event traffic spikes
          datasetConfigManager
              .updateLastRefreshTime(dataset, event.getHighWatermark(), System.currentTimeMillis());
          ThirdeyeMetricsUtil.processedTriggerEventCounter.inc();
          LOG.debug("Finished processing event: " + event.getDatasetName());
        } catch (Exception e) {
          LOG.error("Error in processing event for {}, so skipping...", event.getDatasetName(), e);
        }
      }
    }
    if (!events.isEmpty()) {
      consumer.commitSync();
    } else if (sleepTimeInMilli > 0) {
      LOG.info("Going to sleep and wake up in next " + sleepTimeInMilli + " milliseconds...");
      Thread.sleep(sleepTimeInMilli);
    }
  }

  private boolean checkAllFiltersPassed(DataAvailabilityEvent event) {
    for (DataAvailabilityEventFilter filter : filters) {
      if (!filter.isPassed(event)) {
        return false;
      }
    }
    return true;
  }
}
