/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.kafka;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KafkaProducerTriggerSpec extends AbstractSpec {

  private Map<String, Object> producerConfigs = Collections.emptyMap();
  private String topic;
  private List<String> keyColumns = Collections.emptyList();
  private List<String> valueColumns = Collections.emptyList();

  public Map<String, Object> getProducerConfigs() {
    return producerConfigs;
  }

  public KafkaProducerTriggerSpec setProducerConfigs(final Map<String, Object> producerConfigs) {
    this.producerConfigs = producerConfigs;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public KafkaProducerTriggerSpec setTopic(final String topic) {
    this.topic = topic;
    return this;
  }

  public List<String> getKeyColumns() {
    return keyColumns;
  }

  public KafkaProducerTriggerSpec setKeyColumns(final List<String> keyColumns) {
    this.keyColumns = keyColumns;
    return this;
  }

  public List<String> getValueColumns() {
    return valueColumns;
  }

  public KafkaProducerTriggerSpec setValueColumns(final List<String> valueColumns) {
    this.valueColumns = valueColumns;
    return this;
  }
}
