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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;
import static ai.startree.thirdeye.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.datasource.loader.DefaultMinMaxTimeLoader;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader.Extremum;
import ai.startree.thirdeye.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertInsightsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(AlertInsightsProvider.class);

  private static final Interval NOT_USED_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  // computer clock difference is usually order of seconds - but here taking 1 hour is safe and does not impact the logic
  private static final long COMPUTER_CLOCK_MARGIN_MILLIS = 3600_000;

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final DatasetConfigManager datasetConfigManager;
  private final DefaultMinMaxTimeLoader minMaxTimeLoader;

  @Inject
  public AlertInsightsProvider(final AlertTemplateRenderer alertTemplateRenderer,
      final DatasetConfigManager datasetConfigManager, final DefaultMinMaxTimeLoader minMaxTimeLoader) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.datasetConfigManager = datasetConfigManager;
    this.minMaxTimeLoader = minMaxTimeLoader;
  }

  public AlertInsightsApi getInsights(final AlertDTO alertDTO) {
    try {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alertDTO,
          NOT_USED_INTERVAL);
      final AlertMetadataDTO metadata = templateWithProperties.getMetadata();

      final AlertInsightsApi insights = new AlertInsightsApi().setTemplateWithProperties(
          toAlertTemplateApi(templateWithProperties));
      addDatasetTimes(insights, metadata);

      return insights;
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      // can do better exception handling if necessary - see handleAlertEvaluationException
      throw serverError(ERR_UNKNOWN, e);
    }
  }

  private void addDatasetTimes(@NonNull final AlertInsightsApi insights,
      @NonNull final AlertMetadataDTO metadata) throws Exception {
    final String datasetName = metadata.getDataset().getDataset();
    if (datasetName == null) {
      LOG.warn("Dataset name not found in alert metadata. Cannot fetch start and end time.");
      return;
    }
    final DatasetConfigDTO datasetConfigDTO = datasetConfigManager.findByDataset(datasetName);
    if (datasetConfigDTO == null) {
      LOG.warn(
          "Dataset configuration not found: {}. Dataset not onboarded? Cannot fetch start and end time.",
          datasetName);
      return;
    }

    // fetch dataset interval
    addDatasetStartEndTimes(insights, datasetConfigDTO);
    addDefaultTimes(insights, metadata);
  }

  private void addDatasetStartEndTimes(final AlertInsightsApi insights,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {
    final String dataSource = datasetConfigDTO.getDataSource();
    if (dataSource == null) {
      LOG.warn(
          "Datasource is null in dataset configuration: {}. Could not fetch start and end time.",
          datasetConfigDTO.getDataset());
      return;
    }
    addDatasetStart(insights, datasetConfigDTO);
    addDatasetEnd(insights, datasetConfigDTO);
  }

  private void addDatasetStart(final AlertInsightsApi insights,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {
    final Long datasetMinTime = minMaxTimeLoader.fetchExtremumTime(Extremum.MIN,
        datasetConfigDTO,
        null);
    insights.setDatasetStartTime(datasetMinTime);
  }

  private void addDatasetEnd(final AlertInsightsApi insights,
      final DatasetConfigDTO datasetConfigDTO) throws Exception {
    final Long datasetMaxTime = minMaxTimeLoader.fetchExtremumTime(Extremum.MAX,
        datasetConfigDTO,
        null);
    if (datasetMaxTime == null) {
      return;
    }
    // if there is bad data in the dataset, datasetEndTime can have an incorrect value, bigger than the current time - see TE-860
    final long maximumPossibleEndTime = currentMaximumPossibleEndTime();
    final boolean endTimeLooksLegit = datasetMaxTime <= maximumPossibleEndTime;
    if (endTimeLooksLegit) {
      // nominal case
      insights.setDatasetEndTime(datasetMaxTime);
      return;
    }

    // endTime looks incorrect - re-fetch a safer endTime with a timefilter
    insights.setSuspiciousDatasetEndTime(datasetMaxTime);
    LOG.warn(
        "Dataset maxTime is too big: {}. Current system time: {}.Most likely a data issue in the dataset. Rerunning query with a filter < safeEndTime={} to get a safe maxTime.",
        datasetMaxTime,
        System.currentTimeMillis(),
        maximumPossibleEndTime);
    final Interval safeInterval = new Interval(0L, maximumPossibleEndTime);
    final Long safeMaxTime = minMaxTimeLoader.fetchExtremumTime(Extremum.MAX,
        datasetConfigDTO,
        safeInterval);
    if (safeMaxTime == null) {
      LOG.warn("Could not fetch the maxTime on a safe time interval for dataset {}.",
          datasetConfigDTO.getDataset());
    }
    insights.setDatasetEndTime(safeMaxTime);
  }

  // default times for chart - to call after dataset times are set in insights
  private void addDefaultTimes(final AlertInsightsApi insights, final AlertMetadataDTO metadata) {
    final Long datasetStartTime = insights.getDatasetStartTime();
    final Long datasetEndTime = insights.getDatasetEndTime();
    if (datasetStartTime == null || datasetEndTime == null) {
      // cannot compute
      return;
    }
    final DateTimeZone timeZone = optional(metadata.getTimezone()).map(DateTimeZone::forID)
        .orElse(Constants.DEFAULT_TIMEZONE);
    final Interval datasetInterval = new Interval(datasetStartTime, datasetEndTime, timeZone);
    final Period granularity = isoPeriod(metadata.getGranularity());
    // compute default chart interval todo cyril read user metadata default timeframe setting here once implemented
    final Interval defaultInterval = getDefaultChartInterval(datasetInterval, granularity);
    insights.setDefaultStartTime(defaultInterval.getStartMillis());
    insights.setDefaultEndTime(defaultInterval.getEndMillis());
  }

  public static long currentMaximumPossibleEndTime() {
    return System.currentTimeMillis() + COMPUTER_CLOCK_MARGIN_MILLIS;
  }

  @VisibleForTesting
  protected static Interval getDefaultChartInterval(final @NonNull Interval datasetInterval,
      @NonNull final Period granularity) {
    final DateTime defaultEndDateTime = TimeUtils.floorByPeriod(datasetInterval.getEnd(),
        granularity);

    DateTime defaultStartTime = defaultEndDateTime.minus(defaultChartTimeframe(granularity));
    if (defaultStartTime.getMillis() < datasetInterval.getStartMillis()) {
      defaultStartTime = TimeUtils.floorByPeriod(datasetInterval.getStart(), granularity);
      // first bucket may be incomplete - start from second one
      defaultStartTime = defaultStartTime.plus(granularity);
    }

    return new Interval(defaultStartTime, defaultEndDateTime);
  }

  /**
   * Returns a default Period for the UI timeseries chart timeframe, based on the alert granularity.
   * Rule of thumb.
   */
  private static Period defaultChartTimeframe(final Period alertGranularity) {
    final long granularityMillis = alertGranularity.toStandardDuration().getMillis();
    if (granularityMillis < Period.hours(1).toStandardDuration().getMillis()) {
      return Period.months(1);
    } else if (granularityMillis < Period.days(1).toStandardDuration().getMillis()) {
      return Period.months(6);
    } else if (granularityMillis < Period.weeks(1).toStandardDuration().getMillis()) {
      return Period.years(1);
    } else if (granularityMillis < Period.months(1).toStandardDuration().getMillis()) {
      return Period.years(2);
    }
    return Period.years(4);
  }
}
