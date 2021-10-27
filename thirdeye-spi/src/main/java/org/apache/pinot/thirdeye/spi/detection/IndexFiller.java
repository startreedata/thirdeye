package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.joda.time.Interval;

public interface IndexFiller<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable fillIndex(Interval detectionInterval, DataTable dataTable) throws Exception;
}
