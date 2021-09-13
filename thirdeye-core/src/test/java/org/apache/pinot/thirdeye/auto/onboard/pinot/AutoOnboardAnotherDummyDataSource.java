package org.apache.pinot.thirdeye.auto.onboard.pinot;

import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datasource.AutoOnboard;
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
