package org.apache.pinot.thirdeye.tools;

import static org.apache.pinot.thirdeye.AppUtils.logJvmSettings;
import static org.apache.pinot.thirdeye.tools.Utils.loadDefaultDataSources;

import com.google.inject.Injector;
import org.apache.pinot.thirdeye.worker.ThirdEyeWorker;

public class ThirdEyeWorkerDebug {

  public static void main(String[] args) throws Exception {
    logJvmSettings();

    final ThirdEyeWorker thirdEyeWorker = new ThirdEyeWorker();
    thirdEyeWorker.run(ThirdEyeWorker.buildArgs(args));

    final Injector injector = thirdEyeWorker.getInjector();
    loadDefaultDataSources(injector);
  }
}
