/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
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

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);
  private static final Interval DUMMY_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public AlertDetectionIntervalCalculator(final AlertTemplateRenderer alertTemplateRenderer) {
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public Interval getCorrectedInterval(final AlertApi alertApi, final long taskStartMillis,
      final long taskEndMillis) throws IOException, ClassNotFoundException {
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertApi,
        DUMMY_INTERVAL);
    // alertApi does not have an idea if it's new alert tested in the create alert flow
    final long alertId = alertApi.getId() != null ? alertApi.getId() : -1;

    return getCorrectedInterval(alertId,
        taskStartMillis,
        taskEndMillis,
        templateWithProperties);
  }

  /**
   * Applies delay and granularity flooring to the task start and end.
   *
   *
   * Notes:
   * Assumes that the caller of this method is setting the corrected endTime as the alert
   * lastTimestamp once the alert pipeline has run successfully.
   */
  public Interval getCorrectedInterval(final AlertDTO alertDTO, final long taskStartMillis,
      final long taskEndMillis) throws IOException, ClassNotFoundException {
    // render properties - startTime/endTime not important - objective is to get metadata
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
        DUMMY_INTERVAL);

    return getCorrectedInterval(alertDTO.getId(),
        taskStartMillis,
        taskEndMillis,
        templateWithProperties);
  }

  @NotNull
  @VisibleForTesting
  protected static Interval getCorrectedInterval(final long alertId, final long taskStartMillis,
      final long taskEndMillis, final AlertTemplateDTO templateWithProperties) {
    final DateTimeZone dateTimeZone = Optional.ofNullable(getDateTimeZone(templateWithProperties))
        .orElse(Constants.DEFAULT_TIMEZONE);
    final DateTime taskStart = new DateTime(taskStartMillis, dateTimeZone);
    final DateTime taskEnd = new DateTime(taskEndMillis, dateTimeZone);

    DateTime correctedStart = taskStart;
    DateTime correctedEnd = taskEnd;
    // apply delay correction
    final Period delay = getDelay(templateWithProperties);
    if (delay != null) {
      // if alert has already run: start = lastTimestamp = correctedEnd from last run -> delay was applied
      // if alert has never run: delay is not important if end-delay > start.
      // only apply delay on start if end-delay > start is not true
      correctedEnd = correctedEnd.minus(delay);
      if (correctedEnd.isBefore(correctedStart)) {
        correctedStart = correctedStart.minus(delay);
        LOG.warn(
            "EndTime with delay correction {} is before startTime {}. This can happen if delay configuration is changed to a bigger value. "
                + "Applied delay correction to startTime. Detection may rerun on a timeframe on which it already run with a different config",
            correctedEnd,
            correctedStart);
      }
      LOG.info(
          "Applied delay correction of {} for id {} between {} and {}. Corrected timeframe is between {} and {}",
          delay,
          alertId,
          taskStart,
          taskEnd,
          correctedStart,
          correctedEnd);
    }

    // apply granularity correction
    // granularity correction is compatible with datetimezone, but UTC is hardcoded above
    // given that grouping is performed by Pinot, it is UTC grouping - other dbs may manage timezone-aware grouping
    final Period granularity = getGranularity(templateWithProperties);
    if (granularity != null) {
      // if alert has already run: start = lastTimestamp = correctedEnd = floor(end) and floor(floor(x)) = floor(x)
      // if alert has never run: floor is safe to run to avoid most-left bucket cut
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
    }

    return new Interval(correctedStart, correctedEnd);
  }

  // todo cyril move below to a utils class
  @Nullable
  public static DateTimeZone getDateTimeZone(final AlertTemplateDTO templateWithProperties) {
    return Optional.ofNullable(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getTimezone)
        .map(DateTimeZone::forID)
        .orElse(null);
  }

  @Nullable
  private static Period getDelay(final AlertTemplateDTO templateWithProperties) {
    return Optional.ofNullable(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getDataset)
        .map(DatasetConfigDTO::getCompletenessDelay)
        .map(delayString -> Period.parse(delayString, ISOPeriodFormat.standard()))
        .orElse(null);
  }

  @Nullable
  private static Period getGranularity(final AlertTemplateDTO templateWithProperties) {
    return Optional.ofNullable(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getGranularity)
        .map(granularityString -> Period.parse(granularityString, ISOPeriodFormat.standard()))
        .orElse(null);
  }
}
