package ai.startree.thirdeye.scheduler;

import org.quartz.JobKey;

public class JobUtils {

  public static Long getIdFromJobKey(JobKey jobKey) {
    final String[] tokens = jobKey.getName().split("_");
    final String id = tokens[tokens.length - 1];
    return Long.valueOf(id);
  }
}
