package org.apache.pinot.thirdeye.detection.v2.components.datafetcher;

import org.apache.pinot.thirdeye.detection.v2.results.DataTable;
import org.apache.pinot.thirdeye.spi.detection.spec.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.BaseComponent;

public interface DataFetcher<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable getDataTable() throws Exception;
}
