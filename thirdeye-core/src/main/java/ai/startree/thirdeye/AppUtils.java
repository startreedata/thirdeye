package ai.startree.thirdeye;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppUtils {

  private static final Logger log = LoggerFactory.getLogger(AppUtils.class);

  public static void logJvmSettings() {
    final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    log.info(String.format("JVM (%s) arguments: %s",
        System.getProperty("java.version"),
        runtimeMxBean.getInputArguments()));
  }
}
