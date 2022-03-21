/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.detection;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DetectionJobSchedulerUtils {

  private static final String DAY_FORMAT = "yyyyMMdd";
  private static final String HOUR_FORMAT = "yyyyMMddHH";
  private static final String MINUTE_FORMAT = "yyyyMMddHHmm";

  /**
   * Get date time formatter according to granularity of dataset
   * This is to store the date in the db, in the correct SDF
   */
  public static DateTimeFormatter getDateTimeFormatterForDataset(
      DatasetConfigDTO datasetConfig, DateTimeZone dateTimeZone) {
    String pattern = null;
    TimeSpec timeSpec = ThirdEyeUtils.getTimeSpecFromDatasetConfig(datasetConfig);
    TimeUnit unit = timeSpec.getDataGranularity().getUnit();
    switch (unit) {
      case DAYS:
        pattern = DAY_FORMAT;
        break;
      case MINUTES:
      case SECONDS:
      case MILLISECONDS:
        pattern = MINUTE_FORMAT;
        break;
      case HOURS:
      default:
        pattern = HOUR_FORMAT;
        break;
    }
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern).withZone(dateTimeZone);
    return dateTimeFormatter;
  }

  /**
   * round this time to earlier boundary, depending on granularity of dataset
   * e.g. 12:15pm on HOURLY dataset should be treated as 12pm
   * any dataset with granularity finer than HOUR, will be rounded as per function frequency
   * (assumption is that this is in MINUTES)
   * so 12.53 on 5 MINUTES dataset, with function frequency 15 MINUTES will be rounded to 12.45
   */
  public static long getBoundaryAlignedTimeForDataset(DatasetConfigDTO datasetConfig,
      DateTime dateTime,
      AnomalyFunctionDTO anomalyFunction) {
    TimeSpec timeSpec = ThirdEyeUtils.getTimeSpecFromDatasetConfig(datasetConfig);
    TimeUnit dataUnit = timeSpec.getDataGranularity().getUnit();
    TimeGranularity functionFrequency = anomalyFunction.getFrequency();

    // For nMINUTE level datasets, with frequency defined in nMINUTES in the function, (make sure size doesnt exceed 30 minutes, just use 1 HOUR in that case)
    // Calculate time periods according to the function frequency
    if (dataUnit.equals(TimeUnit.MINUTES) || dataUnit.equals(TimeUnit.MILLISECONDS) || dataUnit
        .equals(TimeUnit.SECONDS)) {
      if (functionFrequency.getUnit().equals(TimeUnit.MINUTES) && (functionFrequency.getSize()
          <= 30)) {
        int minuteBucketSize = functionFrequency.getSize();
        int roundedMinutes = (dateTime.getMinuteOfHour() / minuteBucketSize) * minuteBucketSize;
        dateTime = dateTime.withTime(dateTime.getHourOfDay(), roundedMinutes, 0, 0);
      } else {
        dateTime = getBoundaryAlignedTimeForDataset(dateTime, TimeUnit.HOURS); // default to HOURS
      }
    } else {
      dateTime = getBoundaryAlignedTimeForDataset(dateTime, dataUnit);
    }

    return dateTime.getMillis();
  }

  public static DateTime getBoundaryAlignedTimeForDataset(DateTime dateTime, TimeUnit unit) {
    switch (unit) {
      case DAYS:
        dateTime = dateTime.withTimeAtStartOfDay();
        break;
      case HOURS:
      default:
        dateTime = dateTime.withTime(dateTime.getHourOfDay(), 0, 0, 0);
        break;
    }
    return dateTime;
  }

  /**
   * get bucket size in millis, according to data granularity of dataset
   * Bucket size are 1 HOUR for hourly, 1 DAY for daily
   * For MINUTE level data, bucket size is calculated based on anomaly function frequency
   */
  public static Period getBucketSizePeriodForDataset(DatasetConfigDTO datasetConfig,
      AnomalyFunctionDTO anomalyFunction) {
    Period bucketSizePeriod = null;
    TimeSpec timeSpec = ThirdEyeUtils.getTimeSpecFromDatasetConfig(datasetConfig);
    TimeUnit dataUnit = timeSpec.getDataGranularity().getUnit();
    TimeGranularity functionFrequency = anomalyFunction.getFrequency();

    // For nMINUTE level datasets, with frequency defined in nMINUTES in the function, (make sure size doesnt exceed 30 minutes, just use 1 HOUR in that case)
    // Calculate time periods according to the function frequency
    if (dataUnit.equals(TimeUnit.MINUTES) || dataUnit.equals(TimeUnit.MILLISECONDS) || dataUnit
        .equals(TimeUnit.SECONDS)) {
      if (functionFrequency.getUnit().equals(TimeUnit.MINUTES) && (functionFrequency.getSize()
          <= 30)) {
        bucketSizePeriod = new Period(0, 0, 0, 0, 0, functionFrequency.getSize(), 0, 0);
      } else {
        bucketSizePeriod = getBucketSizePeriodForUnit(TimeUnit.HOURS); // default to 1 HOUR
      }
    } else {
      bucketSizePeriod = getBucketSizePeriodForUnit(dataUnit);
    }
    return bucketSizePeriod;
  }

  public static Period getBucketSizePeriodForUnit(TimeUnit unit) {
    Period bucketSizePeriod = null;
    switch (unit) {
      case DAYS:
        bucketSizePeriod = new Period(0, 0, 0, 1, 0, 0, 0, 0); // 1 DAY
        break;
      case HOURS:
      default:
        bucketSizePeriod = new Period(0, 0, 0, 0, 1, 0, 0, 0); //   1 HOUR
        break;
    }
    return bucketSizePeriod;
  }

  /**
   * Create new entries from last entry to current time,
   * according to time granularity of dataset in case of HOURLY/DAILY,
   * and according to time granularity of function frequency in case of MINUTE level data
   * If it is an HOURLY dataset, run detection for every HOUR
   * If it is a DAILY dataset, run detection for every DAY
   * If it is an n MINUTE level dataset, run detection for every bucket, determined by the frequency
   * field in anomaly function
   */
  public static Map<String, Long> getNewEntries(DateTime currentDateTime,
      DetectionStatusDTO lastEntryForFunction,
      AnomalyFunctionDTO anomalyFunction, DatasetConfigDTO datasetConfig,
      DateTimeZone dateTimeZone) {

    Map<String, Long> newEntries = new LinkedHashMap<>();

    // get current hour/day, depending on granularity of dataset,
    DateTimeFormatter dateTimeFormatterForDataset = DetectionJobSchedulerUtils
        .getDateTimeFormatterForDataset(datasetConfig, dateTimeZone);

    long alignedCurrentMillis =
        DetectionJobSchedulerUtils
            .getBoundaryAlignedTimeForDataset(datasetConfig, currentDateTime, anomalyFunction);
    DateTime alignedDateTime = new DateTime(alignedCurrentMillis, dateTimeZone);

    // if first ever entry, create it with current time
    if (lastEntryForFunction == null) {
      String currentDateString = dateTimeFormatterForDataset.print(alignedDateTime);
      newEntries.put(currentDateString, dateTimeFormatterForDataset.parseMillis(currentDateString));
    } else { // else create all entries from last entry onwards to current time
      DateTime lastDateTime = new DateTime(lastEntryForFunction.getDateToCheckInMS(), dateTimeZone);
      Period bucketSizePeriod = DetectionJobSchedulerUtils
          .getBucketSizePeriodForDataset(datasetConfig, anomalyFunction);
      while (lastDateTime.isBefore(alignedDateTime)) {
        lastDateTime = lastDateTime.plus(bucketSizePeriod);
        newEntries.put(dateTimeFormatterForDataset.print(lastDateTime), lastDateTime.getMillis());
      }
    }
    return newEntries;
  }
}
