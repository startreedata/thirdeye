package org.apache.pinot.thirdeye.spi;

import java.util.Collections;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;

public interface Plugin {

  default Iterable<ThirdEyeDataSource> getDataSources() {
    return Collections.emptyList();
  }
}
