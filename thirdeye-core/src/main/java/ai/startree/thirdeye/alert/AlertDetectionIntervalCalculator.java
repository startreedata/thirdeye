/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

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

@Singleton
public class AlertDetectionIntervalCalculator {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public AlertDetectionIntervalCalculator(final AlertTemplateRenderer alertTemplateRenderer) {
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public Interval getCorrectedInterval(final AlertApi alertApi, final long taskStartMillis,
      final long taskEndMillis) throws IOException, ClassNotFoundException {
    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertApi,
        0L,
        0L);
    // alertApi does not have an idea if it's new alert tested in the create alert flow
    long alertId = alertApi.getId() != null ? alertApi.getId() : -1;

    return getCorrectedInterval(alertId,
        new DateTime(taskStartMillis, DateTimeZone.UTC),
        new DateTime(taskEndMillis, DateTimeZone.UTC),
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
        0L,
        0L);

    return getCorrectedInterval(alertDTO.getId(),
        new DateTime(taskStartMillis, DateTimeZone.UTC),
        new DateTime(taskEndMillis, DateTimeZone.UTC),
        templateWithProperties);
  }

  @NotNull
  @VisibleForTesting
  protected static Interval getCorrectedInterval(final long alertId, final DateTime taskStart,
      final DateTime taskEnd, final AlertTemplateDTO templateWithProperties) {
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
