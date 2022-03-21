/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.pinot.thirdeye.spi.detection.EventTrigger;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerException;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;

/**
 * Absolute change rule detection
 */
public class KafkaProducerTrigger implements EventTrigger<KafkaProducerTriggerSpec> {

  private static ObjectMapper objectMapper = new ObjectMapper();
  private Producer producer;
  private String topic;
  private List<String> keyColumns;
  private List<String> valueColumns;

  @Override
  public void init(final KafkaProducerTriggerSpec spec) {
    this.topic = spec.getTopic();
    this.producer = new KafkaProducer(spec.getProducerConfigs());
    this.keyColumns = spec.getKeyColumns();
    this.valueColumns = spec.getValueColumns();
  }

  @Override
  public void trigger(final List<String> columnNames, final List<ColumnType> columnTypes,
      final Object[] event)
      throws EventTriggerException {
    Map<String, Object> record = DataTable.getRecord(columnNames, event);
    final ProducerRecord kafkaRecord;
    kafkaRecord = generateKafkaRecord(record);
    System.out.println("Producing kafkaRecord = " + kafkaRecord);
    final Future send = this.producer.send(kafkaRecord);
    System.out.println("Producing kafkaRecord = " + kafkaRecord + ", result = " + send);
  }

  @Override
  public void close() {
    producer.close();
  }

  private ProducerRecord generateKafkaRecord(final Map<String, Object> record)
      throws EventTriggerException {

    Map<String, Object> key;
    if (keyColumns == null || keyColumns.isEmpty()) {
      key = null;
    } else {
      key = keyColumns.stream().collect(Collectors.toMap(k -> k, k -> record.get(k)));
    }

    Map<String, Object> value;
    if (valueColumns == null || valueColumns.isEmpty()) {
      value = ImmutableMap.copyOf(record);
    } else {
      value = valueColumns.stream().collect(Collectors.toMap(k -> k, k -> record.get(k)));
    }
    try {
      return new ProducerRecord(topic,
          objectMapper.writeValueAsString(key),
          objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new EventTriggerException(e);
    }
  }
}
