package ai.startree.thirdeye.auto.onboard.pinot;

import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datasource.AutoOnboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoOnboardDummyDataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory.getLogger(AutoOnboardDummyDataSource.class);

  public AutoOnboardDummyDataSource(DataSourceMetaBean meta) {
    super(meta);
  }

  @Override
  public void run() {
  }

  @Override
  public void runAdhoc() {

  }
}
