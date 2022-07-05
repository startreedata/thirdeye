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
package ai.startree.thirdeye.plugins.kafka;

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
