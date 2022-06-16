/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Util class for generated PQL queries (pinot).
 */
public class SqlUtils {

  @Deprecated
  public static String getBetweenClause(DateTime start, DateTime endExclusive, TimeSpec timeSpec,
      final DatasetConfigDTO datasetConfig) {
    TimeGranularity dataGranularity = timeSpec.getDataGranularity();
    long dataGranularityMillis = dataGranularity.toMillis();

    String timeField = timeSpec.getColumnName();
    String timeFormat = timeSpec.getFormat();

    // epoch case
    if (timeFormat == null || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
      long startUnits = (long) Math.ceil(start.getMillis() / (double) dataGranularityMillis);
      long endUnits = (long) Math.ceil(endExclusive.getMillis() / (double) dataGranularityMillis);

      // point query
      if (startUnits == endUnits) {
        return String.format(" %s = %d", timeField, startUnits);
      }

      return String.format(" %s >= %d AND %s < %d", timeField, startUnits, timeField, endUnits);
    }

    // NOTE:
    // this is crazy. epoch rounds up, but timeFormat down
    // we maintain this behavior for backward compatibility.

    DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(timeFormat)
        .withZone(SpiUtils.getDateTimeZone(datasetConfig));
    String startUnits = inputDataDateTimeFormatter.print(start);
    String endUnits = inputDataDateTimeFormatter.print(endExclusive);

    // point query
    if (Objects.equals(startUnits, endUnits)) {
      return String.format(" %s = %s", timeField, startUnits);
    }

    return String.format(" %s >= %s AND %s < %s", timeField, startUnits, timeField, endUnits);
  }
}
