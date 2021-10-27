package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;

public interface NullReplacer {
  DataFrame replaceNulls(DataFrame dataFrame);
}
