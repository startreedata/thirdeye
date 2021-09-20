package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;

public interface DataFetcher<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable getDataTable() throws Exception;
}
