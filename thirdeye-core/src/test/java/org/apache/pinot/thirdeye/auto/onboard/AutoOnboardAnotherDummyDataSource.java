package org.apache.pinot.thirdeye.auto.onboard;

import org.apache.pinot.thirdeye.spi.auto.onboard.AutoOnboard;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.DataSourceMetaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoOnboardAnotherDummyDataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory
      .getLogger(AutoOnboardAnotherDummyDataSource.class);

  public AutoOnboardAnotherDummyDataSource(DataSourceMetaBean meta) {
    super(meta);
  }

  @Override
  public void run() {
  }

  @Override
  public void runAdhoc() {

  }
}
