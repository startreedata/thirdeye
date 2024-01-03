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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.detection.v2.AbstractDataTableImpl;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeResultSetDataTable extends AbstractDataTableImpl {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeResultSetDataTable.class);
  // this counter tracks the object type bug - see comment where it is used. Can be removed once the bug is fixed.
  private final Counter incorrectObjectDataTypeCounter = Metrics.counter("pinot_object_type_bug_total");
  // this counter should always be zero - if it's not, then the type fetching is incorrect, the parsing in this class is not implemented correctly or some behaviour changed in Pinot
  private final Counter unknownDataTypeCounter = Metrics.counter("pinot_unknown_data_type_error_total");
  // this counter should always be zero - if it's not, then the parsing in this class is not implemented correctly or some behaviour changed in Pinot
  private final Counter parsingErrorCounter = Metrics.counter("pinot_value_parsing_error_total");

  private final DataFrame dataFrame;

  public ThirdEyeResultSetDataTable(final ThirdEyeResultSet thirdEyeResultSet) {
    dataFrame = generateDataFrame(thirdEyeResultSet);
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }

  private DataFrame generateDataFrame(final ThirdEyeResultSet thirdEyeResultSet) {
    final DataFrame df = new DataFrame();
    final int rowCount = thirdEyeResultSet.getRowCount();
    // TODO CYRIL check if groupKey is still used
    for (int colIdx = 0; colIdx < thirdEyeResultSet.getGroupKeyLength(); colIdx++) {
      final String columnName = thirdEyeResultSet.getGroupKeyColumnName(colIdx);
      final String[] vals = new String[rowCount];
      for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
        vals[rowIdx] = thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx);
      }
      df.addSeries(columnName, StringSeries.buildFrom(vals));
    }
    for (int colIdx = 0; colIdx < thirdEyeResultSet.getColumnCount(); colIdx++) {
      final String columnName = thirdEyeResultSet.getColumnName(colIdx);
      final ColumnDataType type = thirdEyeResultSet.getColumnType(colIdx).getType();
      switch (type) {
        case BOOLEAN:
          final byte[] bVals = new byte[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            bVals[rowIdx] = boolOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, BooleanSeries.buildFrom(bVals));
          break;
        case INT:
          final long[] iVals = new long[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            iVals[rowIdx] = integerOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, LongSeries.buildFrom(iVals));
          break;
        case LONG:
          final long[] lVals = new long[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            lVals[rowIdx] = longOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, LongSeries.buildFrom(lVals));
          break;
        case FLOAT:
        case DOUBLE:
          final double[] dVals = new double[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            dVals[rowIdx] = doubleOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, DoubleSeries.buildFrom(dVals));
          break;
        case STRING:
          final String[] sVals = new String[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            sVals[rowIdx] = stringOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, StringSeries.buildFrom(sVals));
          break;
        case OBJECT:
          // hotfix for https://github.com/apache/pinot/issues/12091 and https://startree.atlassian.net/browse/TE-1955?focusedCommentId=24634
          // DATETIMECONVERT can return an incorrect type `OBJECT` instead of LONG or STRING
          // in ThirdEye context, we only use DATETIMECONVERT to convert to LONG, so we assume it is a LONG
          // the issue does not happen in pinot 1.0.0. It happens on [1.1.?-ST , ..., 1.1.0-ST.19.3, ... 1.1.0-ST.29, ..., ?]
          LOG.warn(
              "Encountered OBJECT type. This should never happen. Assuming it is caused by a bug in DATETIMECONVERT. See comments of this log in the public codebase. Attempting to parse as a LONG. If an exception is raised downstream, please reach out to support.");
          incorrectObjectDataTypeCounter.increment();
          final long[] oVals = new long[rowCount];
          for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
            oVals[rowIdx] = longOrNull(thirdEyeResultSet, rowIdx, colIdx);
          }
          df.addSeries(columnName, LongSeries.buildFrom(oVals));
          break;
        default:
          unknownDataTypeCounter.increment();
          throw new RuntimeException("Unrecognized column type: " + type
              + ". Supported types are BOOLEAN/INT/LONG/FLOAT/DOUBLE/STRING.");
      }
    }

    return df;
  }

  private byte boolOrNull(final ThirdEyeResultSet thirdEyeResultSet, final int rowIdx,
      final int colIdx) {
    final Boolean aBoolean = thirdEyeResultSet.getBoolean(rowIdx, colIdx);
    if (aBoolean == null) {
      return BooleanSeries.NULL;
    } else {
      return aBoolean ? BooleanSeries.TRUE : BooleanSeries.FALSE;
    }
  }

  private String stringOrNull(final ThirdEyeResultSet thirdEyeResultSet, final int rowIdx,
      final int colIdx) {
    return thirdEyeResultSet.getString(rowIdx, colIdx);
  }

  private double doubleOrNull(final ThirdEyeResultSet thirdEyeResultSet, final int rowIdx,
      final int colIdx) {
    try {
      final Double aDouble = thirdEyeResultSet.getDouble(rowIdx, colIdx);
      return aDouble == null ? DoubleSeries.NULL : aDouble;
    }
    // TODO CYRIL - don't replace by null - throw directly - this may break things for existing table though - need to analyze metrics first
    catch (NumberFormatException e) {
      LOG.error("Could not get value of position {},{}. Replacing by null. Error: ", rowIdx, colIdx,
          e);
      parsingErrorCounter.increment();
      return DoubleSeries.NULL;
    }
  }

  private long longOrNull(final ThirdEyeResultSet thirdEyeResultSet, final int rowIdx,
      final int colIdx) {
    try {
      final Long aLong = thirdEyeResultSet.getLong(rowIdx, colIdx);
      return aLong == null ? LongSeries.NULL : aLong;
    }
    // TODO CYRIL - don't replace by null - throw directly - this may break things for existing table though - need to analyze metrics first
    catch (NumberFormatException e) {
      LOG.error("Could not get value of position {},{}. Replacing by null. Error: ", rowIdx, colIdx,
          e);
      parsingErrorCounter.increment();
      return LongSeries.NULL;
    }
  }

  // parse an integer but returns a long for DataFrame
  private long integerOrNull(final ThirdEyeResultSet thirdEyeResultSet, final int rowIdx,
      final int colIdx) {
    try {
      final Integer anInt = thirdEyeResultSet.getInteger(rowIdx, colIdx);
      return anInt == null ? LongSeries.NULL : anInt;
    } catch (NumberFormatException e) {
      LOG.error("Could not get value of position {},{}. Replacing by null. Error: ", rowIdx, colIdx,
          e);
      parsingErrorCounter.increment();
      return LongSeries.NULL;
    }
  }
}
