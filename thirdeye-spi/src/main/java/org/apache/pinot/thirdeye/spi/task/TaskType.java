package org.apache.pinot.thirdeye.spi.task;

public enum TaskType {
  DATA_QUALITY,              // tasks to detect data quality anomalies
  DETECTION,                 // tasks to detect anomalies
  NOTIFICATION,              // tasks to notify/send alerts to customers regarding anomalies
  ONBOARDING,                // tasks to onboard a new alert
  MONITOR                    // tasks to clean up expired/invalid execution history
}
