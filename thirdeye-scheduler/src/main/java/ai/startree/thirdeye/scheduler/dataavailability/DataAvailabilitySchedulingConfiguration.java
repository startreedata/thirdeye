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
package ai.startree.thirdeye.scheduler.dataavailability;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for DataAvailabilityListener.
 */
public class DataAvailabilitySchedulingConfiguration {

  private String consumerClass;
  private String kafkaBootstrapServers;
  private String kafkaTopic;
  private String kafkaConsumerGroupId;
  private String kafkaConsumerPropPath;
  private int numParallelConsumer = 1; // run one consumer by default
  private int datasetWhitelistUpdateFreqInMin = 30; // update every 30 minutes by default
  private long sleepTimeWhenNoEventInMilli = 30_000; // sleep for 30 secs when no event by default
  private long consumerPollTimeInMilli = 5_000; // consumer wait 5 secs by default for the buffer to be filled
  private List<String> dataSourceWhitelist;
  private List<String> filterClassList;

  // delay time after each run for the scheduler to reduce DB polling
  private long schedulerDelayInSec = TimeUnit.MINUTES.toSeconds(5);

  // default threshold if detection level threshold is not set
  private long taskTriggerFallBackTimeInSec = TimeUnit.DAYS.toSeconds(1);

  // scheduling window for data availability scheduling to avoid over-scheduling if watermarks do not move forward
  private long schedulingWindowInSec = TimeUnit.MINUTES.toSeconds(30);

  // schedule delay upon receiving data update trigger in case the visibility of data in source is delayed
  private long scheduleDelayInSec = TimeUnit.MINUTES.toSeconds(10);

  public String getConsumerClass() {
    return consumerClass;
  }

  public DataAvailabilitySchedulingConfiguration setConsumerClass(final String consumerClass) {
    this.consumerClass = consumerClass;
    return this;
  }

  public String getKafkaBootstrapServers() {
    return kafkaBootstrapServers;
  }

  public DataAvailabilitySchedulingConfiguration setKafkaBootstrapServers(
      final String kafkaBootstrapServers) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
    return this;
  }

  public String getKafkaTopic() {
    return kafkaTopic;
  }

  public DataAvailabilitySchedulingConfiguration setKafkaTopic(final String kafkaTopic) {
    this.kafkaTopic = kafkaTopic;
    return this;
  }

  public String getKafkaConsumerGroupId() {
    return kafkaConsumerGroupId;
  }

  public DataAvailabilitySchedulingConfiguration setKafkaConsumerGroupId(
      final String kafkaConsumerGroupId) {
    this.kafkaConsumerGroupId = kafkaConsumerGroupId;
    return this;
  }

  public String getKafkaConsumerPropPath() {
    return kafkaConsumerPropPath;
  }

  public DataAvailabilitySchedulingConfiguration setKafkaConsumerPropPath(
      final String kafkaConsumerPropPath) {
    this.kafkaConsumerPropPath = kafkaConsumerPropPath;
    return this;
  }

  public int getNumParallelConsumer() {
    return numParallelConsumer;
  }

  public DataAvailabilitySchedulingConfiguration setNumParallelConsumer(
      final int numParallelConsumer) {
    this.numParallelConsumer = numParallelConsumer;
    return this;
  }

  public int getDatasetWhitelistUpdateFreqInMin() {
    return datasetWhitelistUpdateFreqInMin;
  }

  public DataAvailabilitySchedulingConfiguration setDatasetWhitelistUpdateFreqInMin(
      final int datasetWhitelistUpdateFreqInMin) {
    this.datasetWhitelistUpdateFreqInMin = datasetWhitelistUpdateFreqInMin;
    return this;
  }

  public long getSleepTimeWhenNoEventInMilli() {
    return sleepTimeWhenNoEventInMilli;
  }

  public DataAvailabilitySchedulingConfiguration setSleepTimeWhenNoEventInMilli(
      final long sleepTimeWhenNoEventInMilli) {
    this.sleepTimeWhenNoEventInMilli = sleepTimeWhenNoEventInMilli;
    return this;
  }

  public long getConsumerPollTimeInMilli() {
    return consumerPollTimeInMilli;
  }

  public DataAvailabilitySchedulingConfiguration setConsumerPollTimeInMilli(
      final long consumerPollTimeInMilli) {
    this.consumerPollTimeInMilli = consumerPollTimeInMilli;
    return this;
  }

  public List<String> getDataSourceWhitelist() {
    return dataSourceWhitelist;
  }

  public DataAvailabilitySchedulingConfiguration setDataSourceWhitelist(
      final List<String> dataSourceWhitelist) {
    this.dataSourceWhitelist = dataSourceWhitelist;
    return this;
  }

  public List<String> getFilterClassList() {
    return filterClassList;
  }

  public DataAvailabilitySchedulingConfiguration setFilterClassList(
      final List<String> filterClassList) {
    this.filterClassList = filterClassList;
    return this;
  }

  public long getSchedulerDelayInSec() {
    return schedulerDelayInSec;
  }

  public DataAvailabilitySchedulingConfiguration setSchedulerDelayInSec(
      final long schedulerDelayInSec) {
    this.schedulerDelayInSec = schedulerDelayInSec;
    return this;
  }

  public long getTaskTriggerFallBackTimeInSec() {
    return taskTriggerFallBackTimeInSec;
  }

  public DataAvailabilitySchedulingConfiguration setTaskTriggerFallBackTimeInSec(
      final long taskTriggerFallBackTimeInSec) {
    this.taskTriggerFallBackTimeInSec = taskTriggerFallBackTimeInSec;
    return this;
  }

  public long getSchedulingWindowInSec() {
    return schedulingWindowInSec;
  }

  public DataAvailabilitySchedulingConfiguration setSchedulingWindowInSec(
      final long schedulingWindowInSec) {
    this.schedulingWindowInSec = schedulingWindowInSec;
    return this;
  }

  public long getScheduleDelayInSec() {
    return scheduleDelayInSec;
  }

  public DataAvailabilitySchedulingConfiguration setScheduleDelayInSec(
      final long scheduleDelayInSec) {
    this.scheduleDelayInSec = scheduleDelayInSec;
    return this;
  }
}
