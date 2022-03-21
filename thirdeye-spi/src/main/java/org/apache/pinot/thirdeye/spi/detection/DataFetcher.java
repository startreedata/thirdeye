package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.joda.time.Interval;

public interface DataFetcher<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable getDataTable(Interval detectionInterval) throws Exception;
}
