package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.DataFrame;

public interface NullReplacer {
  DataFrame replaceNulls(DataFrame dataFrame);
}
