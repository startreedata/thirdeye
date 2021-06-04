package org.apache.pinot.thirdeye.tools;

import static org.apache.pinot.thirdeye.AppUtils.logJvmSettings;
import static org.apache.pinot.thirdeye.tools.Utils.loadDefaultDataSources;

import com.google.inject.Injector;
import org.apache.pinot.thirdeye.ThirdEyeCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeCoordinatorDebug {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeCoordinatorDebug.class);

  public static void main(String[] args) throws Exception {
    logJvmSettings();

    final ThirdEyeCoordinator thirdEyeCoordinator = new ThirdEyeCoordinator();
    thirdEyeCoordinator.run(args);

    final Injector injector = thirdEyeCoordinator.getInjector();
    loadDefaultDataSources(injector);
  }
}
