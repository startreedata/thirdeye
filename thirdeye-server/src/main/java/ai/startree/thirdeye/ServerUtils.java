/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtils {

  private static final Logger log = LoggerFactory.getLogger(ServerUtils.class);

  public static void logJvmSettings() {
    final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    log.info(String.format("JVM (%s) arguments: %s",
        System.getProperty("java.version"),
        runtimeMxBean.getInputArguments()));
  }
}
