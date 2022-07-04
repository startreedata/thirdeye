/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.detection.anomaly.alert.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertScreenshotHelper {

  private static final Logger LOG = LoggerFactory.getLogger(AlertScreenshotHelper.class);

  private static final String ROOT_DIR = "";
  private static final String PHANTOM_JS_PATH = "";
  private static final String TEMP_PATH = "/tmp/graph";
  private static final String SCREENSHOT_FILE_SUFFIX = ".png";
  private static final String GRAPH_SCREENSHOT_GENERATOR_SCRIPT = "/getGraphPnj.js";
  private static final ExecutorService executorService = Executors.newCachedThreadPool();

  public static String takeGraphScreenShot(final String anomalyId, final String uiPublicUrl) {
    return takeGraphScreenShot(anomalyId, uiPublicUrl,
        ROOT_DIR,
        PHANTOM_JS_PATH);
  }

  public static String takeGraphScreenShot(final String anomalyId, final String dashboardHost,
      final String rootDir,
      final String phantomJsPath) {
    final Callable<String> callable = () -> takeScreenshot(anomalyId, dashboardHost, rootDir,
        phantomJsPath);
    final Future<String> task = executorService.submit(callable);
    String result = null;
    try {
      result = task.get(3, TimeUnit.MINUTES);
      LOG.info("Finished with result: {}", result);
    } catch (final Exception e) {
      LOG.error("Exception in fetching screenshot for anomaly id {}", anomalyId);
    }
    return result;
  }

  private static String takeScreenshot(
      final String anomalyId, final String dashboardHost, final String rootDir,
      final String phantomJsPath) throws Exception {
    final String imgRoute = dashboardHost + "/app/#/screenshot/" + anomalyId;
    LOG.info("imgRoute {}", imgRoute);

    final String phantomScript = rootDir + GRAPH_SCREENSHOT_GENERATOR_SCRIPT;
    LOG.info("Phantom JS script {}", phantomScript);

    final String imgPath = TEMP_PATH + anomalyId + SCREENSHOT_FILE_SUFFIX;
    LOG.info("imgPath {}", imgPath);

    final Process proc = Runtime.getRuntime().exec(
        new String[]{phantomJsPath, "phantomjs", "--ssl-protocol=any", "--ignore-ssl-errors=true",
            phantomScript, imgRoute, imgPath});
    LOG.info("Waiting for phantomjs...");

    final boolean isComplete = proc.waitFor(2, TimeUnit.MINUTES);
    LOG.info("phantomjs complete status after waiting: {}", isComplete);
    if (!isComplete) {
      proc.destroyForcibly();
      throw new Exception("PhantomJS process timeout");
    }
    return imgPath;
  }
}
