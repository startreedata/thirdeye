package org.apache.pinot.thirdeye.spi.detection.v2.components;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;

public interface DataFetcher<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable getDataTable() throws Exception;
}
