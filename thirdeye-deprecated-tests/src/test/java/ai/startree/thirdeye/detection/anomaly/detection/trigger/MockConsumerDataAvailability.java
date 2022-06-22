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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MockConsumerDataAvailability extends DataAvailabilityKafkaConsumer {

  private int callCount;

  public MockConsumerDataAvailability() {
    super("", "", "", new Properties());
    callCount = 0;
  }

  @Override
  public List<DataAvailabilityEvent> poll(long poll) {
    List<DataAvailabilityEvent> res = new ArrayList<>();
    if (callCount == 0) {
      res.add(createEvent(1, 1000, 2000));
      res.add(createEvent(2, 2000, 3000));
    } else if (callCount == 1) {
      res.add(createEvent(1, 2000, 3000));
      res.add(createEvent(1, 2000, 3000));
      res.add(createEvent(1, 1000, 2000));
      res.add(createEvent(2, 1000, 3000));
    } else if (callCount == 2) {
      res.add(createEvent(1, 0, 1000));
      res.add(createEvent(3, 1000, 2000));
      res.add(createEvent(4, 1000, 2000));
    }
    return res;
  }

  @Override
  public void commitSync() {
    callCount += 1;
  }

  @Override
  public void close() {

  }

  private static DataAvailabilityEvent createEvent(int suffix, long lowWatermark,
      long highWatermark) {
    String datasetPrefix = DataAvailabilityEventListenerTest.TEST_DATASET_PREFIX;
    String dataSource = DataAvailabilityEventListenerTest.TEST_DATA_SOURCE;
    MockDataAvailabilityEvent event = new MockDataAvailabilityEvent();
    event.setDatasetName(datasetPrefix + suffix);
    event.setDataStore(dataSource);
    event.setLowWatermark(lowWatermark);
    event.setHighWatermark(highWatermark);
    return event;
  }
}
