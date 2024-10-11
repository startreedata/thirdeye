/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.util;

import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDateTimeZone;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDelay;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getGranularity;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.util.TimeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compute a detection interval, based on task start and end.
 * Applies completenessDelay and monitoringGranularity rounding.
 * Applies timezone.
 *
 * For detections/evaluations, this is the entrypoint that changes long startTime and endTimes into
 * an Interval detectionInterval with a timeZone.
 */
public class DetectionIntervalUtils {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionIntervalUtils.class);

  /**
   * Applies delay and granularity flooring to the task start and end.
   * Notes:
   * Assumes that the caller of this method is setting the corrected endTime as the alert
   * lastTimestamp once the alert pipeline has run successfully.
   */
  @NonNull
  public static Interval computeCorrectedInterval(final @Nullable Long alertId, final long taskStartMillis,
      final long taskEndMillis, final AlertTemplateDTO renderedTemplate) {
    final AlertMetadataDTO metadata = renderedTemplate.getMetadata();
    final Chronology chronology = getDateTimeZone(metadata);
    final DateTime taskStart = new DateTime(taskStartMillis, chronology);
    final DateTime taskEnd = new DateTime(taskEndMillis, chronology);

    DateTime correctedStart = taskStart;
    DateTime correctedEnd = taskEnd;
    // apply delay correction
    final Period delay = getDelay(metadata);
    final DateTime dataWatermark = DateTime.now(chronology).minus(delay);
    final boolean completenessDelayIsPositive = delay.toStandardDuration().getMillis() >= 0;
    if (completenessDelayIsPositive) {
      if (correctedEnd.isAfter(dataWatermark)) {
        correctedEnd = dataWatermark;
        LOG.info(
            "Applied delay correction of {} for id {} between {} and {}. Corrected end time is {}",
            delay, alertId, taskStart, taskEnd, correctedEnd);
      }
      if (correctedEnd.isBefore(correctedStart)) {
        correctedStart = correctedStart.minus(delay);
        LOG.warn(
            "EndTime with delay correction {} is before startTime {}. This can happen if delay configuration is changed to a bigger value. "
                + "Applied delay correction to startTime. Detection may rerun on a timeframe on which it already run with a different config",
            correctedEnd,
            correctedStart);
      }  
    } else {
      // use case: completenessDelay is negative: -6 hours. Cron runs at 18:00. DataWatermark is 18 - -6=24 --> the day can be considered complete 
      // useful if we know the data is ready to analyze at 6pm and the rest of the data is not relevant 
      if (correctedEnd.isBefore(dataWatermark)) {
        correctedEnd = dataWatermark;
        LOG.info(
            "Applied delay correction of {} for id {} between {} and {}. Corrected end time is {}",
            delay, alertId, taskStart, taskEnd, correctedEnd);
      } else {
        LOG.error("Task end is after the data watermark, even if the data watermark is in the future because the completenessDelay is negative. This should not happen, except for detection tasks created manually. Please reach out to support.");
      }
      // should always be true because correctedEnd can only get bigger with the logic above
      checkState(correctedStart.isBefore(correctedEnd));
    }
    

    // apply granularity correction
    final Period granularity = getGranularity(metadata);
    // if alert has already run: start = lastTimestamp = correctedEnd = floor(end) and floor(floor(x)) = floor(x)
    // if alert has never run: floor prevents leftmost bucket cut
    correctedStart = TimeUtils.floorByPeriod(correctedStart, granularity);
    correctedEnd = TimeUtils.floorByPeriod(correctedEnd, granularity);
    LOG.info(
        "Applied granularity correction of {} for id {} between {} and {}. Corrected timeframe is between {} and {}",
        granularity,
        alertId,
        taskStart,
        taskEnd,
        correctedStart,
        correctedEnd);

    return new Interval(correctedStart, correctedEnd);
  }
}
