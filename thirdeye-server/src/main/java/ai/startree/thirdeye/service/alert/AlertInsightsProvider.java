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
package ai.startree.thirdeye.service.alert;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.Constants.UTC_TIMEZONE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASET_NOT_FOUND_IN_NAMESPACE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDelay;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getGranularity;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.timezonesAreEquivalent;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.api.AlertInsightsRequestApi;
import ai.startree.thirdeye.spi.api.AnalysisRunInfo;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Future;
import javax.ws.rs.WebApplicationException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertInsightsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AlertInsightsProvider.class);
  // assumes this time has no DST happening for all timezones - did not check but should be the case
  private static final DateTime TIME_ORIGIN_FOR_TZ_DIFF_1 = new DateTime(2023, 1, 12, 0, 0, 0,
      ISOChronology.getInstanceUTC());
  private static final DateTime TIME_ORIGIN_FOR_TZ_DIFF_2 = new DateTime(2023, 5, 12, 0, 0, 0,
      ISOChronology.getInstanceUTC());
  private static final DateTime TIME_ORIGIN_FOR_TZ_DIFF_3 = new DateTime(2023, 9, 12, 0, 0, 0,
      ISOChronology.getInstanceUTC());

  private static final Interval NOT_USED_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  // computer clock difference is usually order of seconds - but here taking 1 hour is safe and does not impact the logic
  private static final long COMPUTER_CLOCK_MARGIN_MILLIS = 3600_000;
  private static final long FETCH_TIMEOUT_MILLIS = 30_000;

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourceManager dataSourceDao;
  private final MinMaxTimeLoader minMaxTimeLoader;
  final AuthorizationManager authorizationManager;

  @Inject
  public AlertInsightsProvider(final AlertTemplateRenderer alertTemplateRenderer,
      final DatasetConfigManager datasetConfigManager, final DataSourceManager dataSourceManager,
      final MinMaxTimeLoader minMaxTimeLoader,
      final AuthorizationManager authorizationManager) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourceDao = dataSourceManager;
    this.minMaxTimeLoader = minMaxTimeLoader;
    this.authorizationManager = authorizationManager;
  }

  public AlertInsightsApi getInsights(final ThirdEyePrincipal principal,
      final AlertInsightsRequestApi request) {
    // fixme cyril add authz on template, dataset, datasource, etc - next PR - requires redesign
    final AlertApi alertApi = request.getAlert();
    final String namespace = authorizationManager.currentNamespace(principal);
    try {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertApi,
          NOT_USED_INTERVAL);
      return buildInsights(templateWithProperties, namespace);
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      // can do better exception handling if necessary - see handleAlertEvaluationException
      throw serverError(ERR_UNKNOWN, e);
    }
  }

  /**
   * This method is not responsible for checking authz of the alertDto
   */
  public AlertInsightsApi getInsights(final ThirdEyePrincipal principal, final AlertDTO alertDTO) {
    // fixme cyril add authz on template, dataset, datasource, etc  - next PR - requires redesign
    authorizationManager.enrichNamespace(principal, alertDTO);
    try {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
          NOT_USED_INTERVAL);
      return buildInsights(templateWithProperties, alertDTO.namespace());
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      // can do better exception handling if necessary - see handleAlertEvaluationException
      throw serverError(ERR_UNKNOWN, e);
    }
  }

  private AlertInsightsApi buildInsights(final AlertTemplateDTO templateWithProperties,
      final @Nullable String namespace)
      throws Exception {
    final AlertMetadataDTO metadata = templateWithProperties.getMetadata();

    final AlertInsightsApi insights = new AlertInsightsApi().setAnalysisRunInfo(
            AnalysisRunInfo.success())
        .setTemplateWithProperties(toAlertTemplateApi(templateWithProperties));
    addDatasetTimes(insights, metadata, namespace);

    return insights;
  }

  private void addDatasetTimes(@NonNull final AlertInsightsApi insights,
      @NonNull final AlertMetadataDTO metadata, final @Nullable String namespace) throws Exception {
    final String datasetName = metadata.getDataset().getDataset();
    if (datasetName == null) {
      throw new ThirdEyeException(ERR_MISSING_CONFIGURATION_FIELD,
          "Dataset name not found in alert metadata.");
    }
    DatasetConfigDTO datasetConfigDTO = datasetConfigManager.findByNameAndNamespaceOrUnsetNamespace(
        datasetName, namespace);
    ensureExists(datasetConfigDTO, ERR_DATASET_NOT_FOUND_IN_NAMESPACE, datasetName, namespace);

    // TODO CYRIL add authz - later inject datasource id in datasetConfig and use it instead of fetching by name/namespace
    final DataSourceDTO dataSourceDTO = dataSourceDao.findUniqueByNameAndNamespace(datasetConfigDTO.getDataSource(), datasetConfigDTO.namespace());
    ensureExists(dataSourceDTO, ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, datasetName + " in namespace " + datasetConfigDTO.namespace());

    // fetch dataset interval
    addDatasetStartEndTimes(insights, dataSourceDTO, datasetConfigDTO);
    addDefaults(insights, metadata);
  }

  private void addDatasetStartEndTimes(final AlertInsightsApi insights,
      final DataSourceDTO dataSourceDTO,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {
    // launch min, max, safeMax queries async
    final Future<@Nullable Long> minTimeFuture = minMaxTimeLoader.fetchMinTimeAsync(
        dataSourceDTO, datasetConfigDTO, null);
    final Future<@Nullable Long> maxTimeFuture = minMaxTimeLoader.fetchMaxTimeAsync(
        dataSourceDTO, datasetConfigDTO, null);
    final long maximumPossibleEndTime = currentMaximumPossibleEndTime();
    final Interval safeInterval = new Interval(0L, maximumPossibleEndTime);
    final Future<@Nullable Long> safeMaxTimeFuture = minMaxTimeLoader.fetchMaxTimeAsync(
        dataSourceDTO, datasetConfigDTO, safeInterval);

    // process futures
    // process startTime
    final Long datasetStartTime = minTimeFuture.get(FETCH_TIMEOUT_MILLIS, MILLISECONDS);
    if (datasetStartTime == null) {
      insights.setAnalysisRunInfo(AnalysisRunInfo.failure(
          String.format("Failed to fetch dataset start time. Table %s is empty or unavailable?",
              datasetConfigDTO.getDataset())));
      return;
    }
    insights.setDatasetStartTime(datasetStartTime);

    // process endTime
    final @Nullable Long datasetMaxTime = maxTimeFuture.get(FETCH_TIMEOUT_MILLIS, MILLISECONDS);
    if (datasetMaxTime == null) {
      insights.setAnalysisRunInfo(AnalysisRunInfo.failure(
          String.format("Failed to fetch dataset end time. Table %s is empty or unavailable?",
              datasetConfigDTO.getDataset())));
    } else if (datasetMaxTime <= maximumPossibleEndTime) {
      insights.setDatasetEndTime(datasetMaxTime);
    } else {
      // there is bad data in the dataset, datasetMaxTime has an incorrect value, bigger than the current time - see TE-860
      // use the safe endTime
      insights.setSuspiciousDatasetEndTime(datasetMaxTime);
      LOG.warn(
          "Dataset maxTime is too big: {}. Current system time: {}.Most likely a data issue in the dataset. Rerunning query with a filter < safeEndTime={} to get a safe maxTime.",
          datasetMaxTime, System.currentTimeMillis(), maximumPossibleEndTime);
      final @Nullable Long safeMaxTime = safeMaxTimeFuture.get(FETCH_TIMEOUT_MILLIS, MILLISECONDS);
      if (safeMaxTime == null) {
        insights.setAnalysisRunInfo(AnalysisRunInfo.failure(String.format(
            "Failed to fetch dataset safe end time. The time configuration of the table %s may be incorrect.",
            datasetConfigDTO.getDataset())));
      } else {
        insights.setDatasetEndTime(safeMaxTime);
      }
    }
  }

  // default times for chart - to call after dataset times are set in insights
  private void addDefaults(final AlertInsightsApi insights, final AlertMetadataDTO metadata) {
    if (!insights.getAnalysisRunInfo().isSuccess()) {
      return;
    }
    final @NonNull Long datasetStartTime = insights.getDatasetStartTime();
    final @NonNull Long datasetEndTime = insights.getDatasetEndTime();
    final Chronology chronology = optional(metadata.getTimezone()).map(DateTimeZone::forID)
        .map(ISOChronology::getInstance)
        .map(e -> (Chronology) e)
        .orElse(Constants.DEFAULT_CHRONOLOGY);
    final Interval datasetInterval = new Interval(datasetStartTime, datasetEndTime, chronology);
    final Period granularity = getGranularity(metadata);
    final Period completenessDelay = getDelay(metadata);
    final String defaultCron = defaultCronFor(granularity.toString(), chronology,
        completenessDelay);
    insights.setDefaultCron(defaultCron);
    // compute default chart interval
    final Interval defaultInterval = getDefaultChartInterval(datasetInterval, granularity);
    if (defaultInterval.toDurationMillis() == 0) {
      insights.setAnalysisRunInfo(AnalysisRunInfo.failure(String.format(
          "Default interval start is equal to default interval end: %s. There is not enough data or the granularity %s is too big.",
          defaultInterval.getStartMillis(), granularity)));
    }
    insights.setDefaultStartTime(defaultInterval.getStartMillis());
    insights.setDefaultEndTime(defaultInterval.getEndMillis());
  }

  public static long currentMaximumPossibleEndTime() {
    return System.currentTimeMillis() + COMPUTER_CLOCK_MARGIN_MILLIS;
  }

  @VisibleForTesting
  protected static @Nullable String defaultCronFor(final String granularity,
      final Chronology chronology, final Period completenessDelay) {
    Period roundedCompletenessDelay = completenessDelay;
    if (completenessDelay.getMillis() != 0) {
      // ceil to the nearest second
      roundedCompletenessDelay = completenessDelay.withMillis(0)
          .withSeconds(completenessDelay.getSeconds() + 1);
    }
    final Duration completenessDuration = roundedCompletenessDelay.toStandardDuration();
    long completenessHours = completenessDuration.getStandardHours() % 24;
    long completenessMinutes = completenessDuration.getStandardMinutes() % 60;
    long completenessSeconds = completenessDuration.getStandardSeconds() % 60;

    switch (granularity) {
      case "PT1M":
        return String.format("%s * * * * ? *", completenessSeconds);
      case "PT5M":
      case "PT10M":
      case "PT15M":
      case "PT30M":
        final String everyNMinutes = granularity.substring(2, granularity.length() - 1);
        return String.format("%s %s/%s * * * ? *", completenessSeconds,
            completenessMinutes % Integer.parseInt(everyNMinutes), everyNMinutes);
      case "PT1H":
        final int zoneOffsetSeconds =
            chronology.getZone().getOffset(TIME_ORIGIN_FOR_TZ_DIFF_1) / 1000;
        final long minutesOfOffset = (zoneOffsetSeconds / 60) % 60;
        final long cronMinutes = (completenessMinutes - minutesOfOffset + 60)
            % 60; // FIXME CYRIL PLUS OR MINUTE FOR THE OFFSET - need to do 45 to know
        return String.format("%s %s * * * ? *", completenessSeconds, cronMinutes);
      case "P1D":
        ;
        long minutesOfOffset1 = 0;
        long hoursOfOffset1 = 0;
        if (!timezonesAreEquivalent(chronology.getZone().toString(), UTC_TIMEZONE)) {
          // need to check if there is DST in the country - take 3 offsets at different times in the year to find offsets
          final int zoneOffset1 = chronology.getZone().getOffset(TIME_ORIGIN_FOR_TZ_DIFF_1);
          final int zoneOffset2 = chronology.getZone().getOffset(TIME_ORIGIN_FOR_TZ_DIFF_2);
          final int zoneOffset3 = chronology.getZone().getOffset(TIME_ORIGIN_FOR_TZ_DIFF_3);
          // assume all offsets have the same sign - as of 2023 it is the case and I don't think this will ever change. Only use case would be if some country changes to -00:30 | +00:30
          final int smallestOffsetSeconds =
              Math.min(Math.min(zoneOffset1, zoneOffset2), zoneOffset3) / 1000;
          minutesOfOffset1 = (smallestOffsetSeconds / 60) % 60;
          hoursOfOffset1 = (smallestOffsetSeconds / 3600) % 24;
        }
        hoursOfOffset1 -= (completenessMinutes - minutesOfOffset1) / 60;
        long cronMinutes1 = (completenessMinutes - minutesOfOffset1) % 60;
        if (cronMinutes1 < 0) {
          cronMinutes1 += 60;
          hoursOfOffset1++;
        }
        final long cronHours1 = (completenessHours - hoursOfOffset1 + 24) % 24;
        return String.format("%s %s %s * * ? *", completenessSeconds, cronMinutes1, cronHours1);
      default:
        // P7D is a bit tricky to implement and no users use P7D, so it's not implemented
        LOG.warn(
            "Unusual granularity {}. Consider reaching out to ThirdEye team to improve the support of this granularity.",
            granularity);
        return null;
    }
  }

  @VisibleForTesting
  protected static Interval getDefaultChartInterval(final @NonNull Interval datasetInterval,
      @NonNull final Period granularity) {
    final DateTime datasetEndTimeBucketStart = TimeUtils.floorByPeriod(datasetInterval.getEnd(),
        granularity);

    DateTime defaultStartTime = datasetEndTimeBucketStart.minus(defaultChartTimeframe(granularity));
    if (defaultStartTime.getMillis() < datasetInterval.getStartMillis()) {
      defaultStartTime = TimeUtils.floorByPeriod(datasetInterval.getStart(), granularity);
      // first bucket may be incomplete - start from second one
      defaultStartTime = defaultStartTime.plus(granularity);
    }

    final DateTime datasetEndTimeBucketEnd = datasetEndTimeBucketStart.plus(granularity);
    return new Interval(defaultStartTime, datasetEndTimeBucketEnd);
  }

  /**
   * Returns a default Period for the UI timeseries chart timeframe, based on the alert granularity.
   * Rule of thumb.
   */
  @VisibleForTesting
  protected static Period defaultChartTimeframe(final Period alertGranularity) {
    if (alertGranularity.getMonths() != 0 || alertGranularity.getYears() != 0) {
      return Period.years(4);
    }
    final long granularityMillis = alertGranularity.toStandardDuration().getMillis();
    if (granularityMillis <= Period.millis(10).toStandardDuration().getMillis()) {
      // for PT0.01S granularity: 2000 points
      return Period.seconds(20);
    } else if (granularityMillis <= Period.millis(100).toStandardDuration().getMillis()) {
      // for PT0.1S granularity: 2400 points
      return Period.minutes(4);
    } else if (granularityMillis <= Period.seconds(1).toStandardDuration().getMillis()) {
      // for PT1S granularity: 1800 points
      return Period.minutes(30);
    } else if (granularityMillis <= Period.seconds(15).toStandardDuration().getMillis()) {
      // for PT15S granularity: 1920 points
      return Period.hours(8);
    } else if (granularityMillis <= Period.minutes(1).toStandardDuration().getMillis()) {
      // for PT1M granularity: 2880 points
      return Period.days(2);
    } else if (granularityMillis <= Period.minutes(5).toStandardDuration().getMillis()) {
      // for PT5M granularity: 2016 points
      return Period.days(7);
    } else if (granularityMillis <= Period.minutes(15).toStandardDuration().getMillis()) {
      // for PT15M granularity: 1344 points
      return Period.days(14);
    } else if (granularityMillis <= Period.hours(1).toStandardDuration().getMillis()) {
      // for PT1H granularity: 1440 points
      return Period.months(2);
    } else if (granularityMillis <= Period.days(1).toStandardDuration().getMillis()) {
      // for P1D granularity: 180 points
      return Period.months(6);
    } else if (granularityMillis <= Period.weeks(1).toStandardDuration().getMillis()) {
      // for P7D granularity: 52 points
      return Period.years(2);
    }
    // for monthly granularity: 36 points
    return Period.years(3);
  }
}
