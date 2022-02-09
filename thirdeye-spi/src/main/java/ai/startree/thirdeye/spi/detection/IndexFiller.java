package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import org.joda.time.Interval;

public interface IndexFiller<T extends AbstractSpec> extends BaseComponent<T> {

  DataTable fillIndex(Interval detectionInterval, DataTable dataTable) throws Exception;
}
