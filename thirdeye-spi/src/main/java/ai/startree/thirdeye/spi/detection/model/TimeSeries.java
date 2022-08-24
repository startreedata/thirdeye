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
package ai.startree.thirdeye.spi.detection.model;

import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import com.google.common.base.Preconditions;

/**
 * Time series. wrapper object of data frame. Used by baselineProvider to return the predicted time
 * series
 */
public class TimeSeries {

  private final DataFrame df;

  private TimeSeries() {
    this.df = new DataFrame();
  }

  /**
   * the size of the time series
   *
   * @return the size of the time series (number of data points)
   */
  public int size() {
    return this.df.size();
  }

  /**
   * Add the series into TimeSeries if it exists in the DataFrame.
   *
   * @param df The source DataFrame.
   * @param name The series name.
   */
  private static void addSeries(TimeSeries ts, DataFrame df, String name) {
    if (df.contains(name)) {
      ts.df.addSeries(name, df.get(name));
    }
  }

  /**
   * return a empty time series
   *
   * @return a empty time series
   */
  public static TimeSeries empty() {
    TimeSeries ts = new TimeSeries();
    ts.df.addSeries(Constants.COL_TIME, LongSeries.empty())
        .addSeries(Constants.COL_VALUE, DoubleSeries.empty())
        .addSeries(Constants.COL_CURRENT, DoubleSeries.empty())
        .addSeries(COL_UPPER_BOUND, DoubleSeries.empty())
        .addSeries(COL_LOWER_BOUND, DoubleSeries.empty())
        .setIndex(Constants.COL_TIME);
    return ts;
  }

  /**
   * Add DataFrame into TimeSeries.
   *
   * @param df The source DataFrame.
   * @return TimeSeries that contains the predicted values.
   */
  public static TimeSeries fromDataFrame(DataFrame df) {
    Preconditions.checkArgument(df.contains(Constants.COL_TIME));
    Preconditions.checkArgument(df.contains(Constants.COL_VALUE));
    TimeSeries ts = new TimeSeries();
    // time stamp
    ts.df.addSeries(Constants.COL_TIME, df.get(Constants.COL_TIME)).setIndex(Constants.COL_TIME);
    // predicted baseline values
    addSeries(ts, df, Constants.COL_VALUE);
    // current values
    addSeries(ts, df, Constants.COL_CURRENT);
    // upper bound
    addSeries(ts, df, COL_UPPER_BOUND);
    // lower bound
    addSeries(ts, df, COL_LOWER_BOUND);
    return ts;
  }

  public DoubleSeries getCurrent() {
    return this.df.getDoubles(Constants.COL_CURRENT);
  }

  public LongSeries getTime() {
    return this.df.getLongs(Constants.COL_TIME);
  }

  public DoubleSeries getPredictedBaseline() {
    return this.df.getDoubles(Constants.COL_VALUE);
  }

  public boolean hasUpperBound() {
    return df.contains(COL_UPPER_BOUND);
  }

  public DoubleSeries getPredictedUpperBound() {
    return this.df.getDoubles(COL_UPPER_BOUND);
  }

  public boolean hasLowerBound() {
    return df.contains(COL_LOWER_BOUND);
  }

  public DoubleSeries getPredictedLowerBound() {
    return this.df.getDoubles(COL_LOWER_BOUND);
  }

  public DataFrame getDataFrame() {
    return df;
  }

  @Override
  public String toString() {
    return "TimeSeries{" + "df=" + df + '}';
  }
}
