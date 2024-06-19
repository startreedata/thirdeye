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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDateTimeZone;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDelay;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getGranularity;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;
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
@Singleton
public class AlertDetectionIntervalCalculator {

  private static final Logger LOG = LoggerFactory.getLogger(AlertDetectionIntervalCalculator.class);
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public AlertDetectionIntervalCalculator(final AlertTemplateRenderer alertTemplateRenderer) {
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public Interval getCorrectedInterval(final AlertApi alertApi, final long taskStartMillis,
      final long taskEndMillis) throws IOException, ClassNotFoundException {
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertApi);
    // alertApi does not have an idea if it's new alert tested in the create alert flow
    final long alertId = alertApi.getId() != null ? alertApi.getId() : -1;

    return getCorrectedInterval(alertId,
        taskStartMillis,
        taskEndMillis,
        templateWithProperties.getMetadata());
  }

  /**
   * Applies delay and granularity flooring to the task start and end.
   * Notes:
   * Assumes that the caller of this method is setting the corrected endTime as the alert
   * lastTimestamp once the alert pipeline has run successfully.
   */
  public Interval getCorrectedInterval(final AlertDTO alertDTO, final long taskStartMillis,
      final long taskEndMillis) throws IOException, ClassNotFoundException {
    // render properties - startTime/endTime not important - objective is to get metadata
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO);

    return getCorrectedInterval(alertDTO.getId(),
        taskStartMillis,
        taskEndMillis,
        templateWithProperties.getMetadata());
  }

  @NonNull
  private static Interval getCorrectedInterval(final long alertId, final long taskStartMillis,
      final long taskEndMillis, final AlertMetadataDTO metadata) {
    final Chronology chronology = getDateTimeZone(metadata);
    final DateTime taskStart = new DateTime(taskStartMillis, chronology);
    final DateTime taskEnd = new DateTime(taskEndMillis, chronology);

    DateTime correctedStart = taskStart;
    DateTime correctedEnd = taskEnd;
    // apply delay correction
    final Period delay = getDelay(metadata);
    final DateTime dataWatermark = DateTime.now(chronology).minus(delay);
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
