package org.apache.pinot.thirdeye.spi.task;

public enum TaskStatus {
  WAITING,
  RUNNING,
  COMPLETED,
  FAILED,
  TIMEOUT
}
