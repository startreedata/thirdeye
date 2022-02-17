/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.detection.trigger;

public interface DataAvailabilityEvent {

  enum Status {
    FINISHED,  // data load finished
    FAILED,  // data load failed
    NONE  // default value
  }

  /**
   * @return the status of event
   */
  Status getStatus();

  /**
   * @return the name of the dataset
   */
  String getDatasetName();

  /**
   * @return the name of data source
   */
  String getDataSource();

  /**
   * @return the namespace of data
   */
  String getNamespace();

  /**
   * @return the smallest timestamp for the data event in epoch time (milliseconds)
   */
  long getLowWatermark();

  /**
   * @return the largest timestamp for the data event in epoch time (milliseconds)
   */
  long getHighWatermark();

  /**
   * @return the timestamp of event generated in epoch time (milliseconds)
   */
  long getEventTime();
}
