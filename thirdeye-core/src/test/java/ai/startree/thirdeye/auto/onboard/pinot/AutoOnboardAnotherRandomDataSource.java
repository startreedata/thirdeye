package ai.startree.thirdeye.auto.onboard.pinot;

import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datasource.AutoOnboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoOnboardAnotherRandomDataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory
      .getLogger(AutoOnboardAnotherRandomDataSource.class);

  public AutoOnboardAnotherRandomDataSource(DataSourceMetaBean meta) {
    super(meta);
  }

  @Override
  public void run() {
    throw new RuntimeException("There was an exception while executing this Source");
  }

  @Override
  public void runAdhoc() {

  }
}
