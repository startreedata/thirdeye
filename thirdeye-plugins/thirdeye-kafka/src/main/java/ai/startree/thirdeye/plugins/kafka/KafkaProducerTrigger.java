/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.kafka;

import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerException;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
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
