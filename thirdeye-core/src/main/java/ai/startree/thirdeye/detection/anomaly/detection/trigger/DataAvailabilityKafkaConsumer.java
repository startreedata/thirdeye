/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.detection.trigger;

import java.util.List;
import java.util.Properties;

/**
 * This abstract class defines the framework for Kafka consumers that generates trigger event for
 * anomaly detection.
 * By extending from this abstract class, an application-specific consumer can be used in {@code
 * DataAvailabilityEventListener},
 * so that anomaly detection can be triggered regardless of the actual content of Kafka events. In
 * order to be
 * initialized by {@code DataAvailabilityEventListener}, all extended consumer classes should have
 * the same constructor of this
 * base class.
 */
public abstract class DataAvailabilityKafkaConsumer {

  protected final String _topic;
  protected final String _groupId;
  protected final String _bootstrapServers;
  protected final Properties _properties;

  public DataAvailabilityKafkaConsumer(String topic, String groupId, String bootstrapServers,
      Properties properties) {
    this._topic = topic;
    this._groupId = groupId;
    this._bootstrapServers = bootstrapServers;
    this._properties = properties;
  }

  /**
   * Get a batch of events from subscribed Kafka topic
   *
   * @param poll poll time for Kafka consumer
   * @return list of events from Source
   */
  public abstract List<DataAvailabilityEvent> poll(long poll);

  /**
   * Commit a checkpoint
   */
  public abstract void commitSync();

  /**
   * Close the consumer
   */
  public abstract void close();
}
