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

public class MockDataAvailabilityEvent implements DataAvailabilityEvent {

  private Status status;
  private String datasetName;
  private String dataStore;
  private String namespace;
  private long lowWatermark;
  private long highWatermark;
  private long eventTime;

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  public void setDataStore(String dataStore) {
    this.dataStore = dataStore;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setLowWatermark(long lowWatermark) {
    this.lowWatermark = lowWatermark;
  }

  public void setHighWatermark(long highWatermark) {
    this.highWatermark = highWatermark;
  }

  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public String getDatasetName() {
    return datasetName;
  }

  @Override
  public String getDataSource() {
    return dataStore;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public long getLowWatermark() {
    return lowWatermark;
  }

  @Override
  public long getHighWatermark() {
    return highWatermark;
  }

  @Override
  public long getEventTime() {
    return eventTime;
  }
}
