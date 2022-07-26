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
package ai.startree.thirdeye.detection.anomaly.monitor;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.util.concurrent.TimeUnit;

public class MonitorConstants {

  public static int DEFAULT_RETENTION_DAYS = 30;
  public static int DEFAULT_COMPLETED_JOB_RETENTION_DAYS = 14;
  public static int DEFAULT_DETECTION_STATUS_RETENTION_DAYS = 7;
  public static int DEFAULT_RAW_ANOMALY_RETENTION_DAYS = 30;
  public static TimeGranularity DEFAULT_MONITOR_FREQUENCY = new TimeGranularity(1, TimeUnit.DAYS);
}
