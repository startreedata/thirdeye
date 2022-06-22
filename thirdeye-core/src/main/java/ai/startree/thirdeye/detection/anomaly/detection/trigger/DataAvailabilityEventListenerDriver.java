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
package ai.startree.thirdeye.detection.anomaly.detection.trigger;

import ai.startree.thirdeye.detection.anomaly.detection.trigger.filter.DataAvailabilityEventFilter;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DatasetTriggerInfoRepo;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is to start DataAvailabilityEventListener based on configuration.
 */
@Singleton
public class DataAvailabilityEventListenerDriver {

  private static final Logger LOG = LoggerFactory
      .getLogger(DataAvailabilityEventListenerDriver.class);

  private final ExecutorService executorService;
  private final DataAvailabilitySchedulingConfiguration config;
  private final Properties consumerProps = new Properties();
  private final List<DataAvailabilityEventListener> listeners = new ArrayList<>();
  private final DatasetConfigManager datasetConfigManager;
  private final DatasetTriggerInfoRepo datasetTriggerInfoRepo;

  @Inject
  public DataAvailabilityEventListenerDriver(DataAvailabilitySchedulingConfiguration config,
      final DatasetConfigManager datasetConfigManager,
      final DatasetTriggerInfoRepo datasetTriggerInfoRepo) {
    this.config = config;
    this.datasetConfigManager = datasetConfigManager;
    this.datasetTriggerInfoRepo = datasetTriggerInfoRepo;
    this.executorService = Executors.newFixedThreadPool(this.config.getNumParallelConsumer(),
        new ThreadFactoryBuilder()
            .setNameFormat("data-avail-event-consumer-%d")
            .build());
  }

  public void start() {
    final String rootDir = System.getProperty("dw.rootDir");
    try {
      this.consumerProps
          .load(new FileInputStream(rootDir + "/" + this.config.getKafkaConsumerPropPath()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    datasetTriggerInfoRepo
        .init(config.getDataSourceWhitelist());

    for (int i = 0; i < config.getNumParallelConsumer(); i++) {
      DataAvailabilityKafkaConsumer consumer = loadConsumer();
      List<DataAvailabilityEventFilter> filters = loadFilters();
      DataAvailabilityEventListener listener = new DataAvailabilityEventListener(consumer,
          filters,
          config.getSleepTimeWhenNoEventInMilli(),
          config.getConsumerPollTimeInMilli(),
          datasetConfigManager,
          datasetTriggerInfoRepo);
      listeners.add(listener);
      executorService.submit(listener);
    }
    LOG.info("Started {} DataAvailabilityEventListener...", listeners.size());
  }

  public void shutdown() {
    listeners.forEach(DataAvailabilityEventListener::close);
    executorService.shutdown();
    LOG.info("Successfully shut down all listeners.");
  }

  private DataAvailabilityKafkaConsumer loadConsumer() {
    String className = config.getConsumerClass();
    try {
      Constructor<?> constructor = Class.forName(className)
          .getConstructor(String.class, String.class, String.class, Properties.class);
      LOG.info("Loaded consumer class: {}", className);
      return (DataAvailabilityKafkaConsumer) constructor.newInstance(config.getKafkaTopic(),
          config.getKafkaConsumerGroupId(), config.getKafkaBootstrapServers(), consumerProps);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to initialize consumer.", e.getCause());
    }
  }

  private List<DataAvailabilityEventFilter> loadFilters() {
    List<DataAvailabilityEventFilter> filters = new ArrayList<>(config.getFilterClassList().size());
    for (String filterClassName : config.getFilterClassList()) {
      try {
        DataAvailabilityEventFilter filter = (DataAvailabilityEventFilter) Class
            .forName(filterClassName).newInstance();
        filters.add(filter);
        LOG.info("Loaded event filter: {}", filterClassName);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("Failed to initialize trigger event filter.",
            e.getCause());
      }
    }
    return filters;
  }
}
