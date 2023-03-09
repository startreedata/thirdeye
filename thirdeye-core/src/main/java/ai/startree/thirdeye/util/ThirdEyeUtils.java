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
package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.Constants;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThirdEyeUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeUtils.class);

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be upto 4 decimals
   * For values gte 0.1, use ##.## (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values lt 0.1 and gte 0.01, use ##.### (eg. 0.08, 0.071, 0.0123)
   * For values lt 0.01 and gte 0.001, use ##.#### (eg. 0.001, 0.00367)
   * This function ensures we don't prematurely round off double values to a fixed format, and make
   * it 0.00 or lose out information
   */
  public static String getRoundedValue(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      if (Double.isNaN(value)) {
        return Double.toString(Double.NaN);
      }
      if (value > 0) {
        return Double.toString(Double.POSITIVE_INFINITY);
      } else {
        return Double.toString(Double.NEGATIVE_INFINITY);
      }
    }
    StringBuffer decimalFormatBuffer = new StringBuffer(Constants.TWO_DECIMALS_FORMAT);
    double compareValue = 0.1;
    while (value > 0 && value < compareValue && !decimalFormatBuffer.toString().equals(
        Constants.MAX_DECIMALS_FORMAT)) {
      decimalFormatBuffer.append(Constants.DECIMALS_FORMAT_TOKEN);
      compareValue = compareValue * 0.1;
    }
    DecimalFormat decimalFormat = new DecimalFormat(decimalFormatBuffer.toString());

    return decimalFormat.format(value);
  }

  /**
   * Safely and quietly shutdown executor service. This method waits until all threads are
   * complete,
   * or timeout occurs or the current thread is interrupted, whichever happens first.
   *
   * <a
   * href="https://stackoverflow.com/questions/10504172/how-to-shutdown-an-executorservice">...</a>
   *
   * @param executorService the executor service to be shutdown.
   */
  public static void shutdownExecutionService(ExecutorService executorService) {
    final Duration timeout = Duration.ofSeconds(15);
    if (executorService == null) {
      return;
    }

    // Prevent new tasks from being submitted
    executorService.shutdown();
    try {
      // wait for tasks to drain
      if (!executorService.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS)) {

        // failed to terminate. shutdown all active tasks now.
        final List<Runnable> runnables = executorService.shutdownNow();
        if (!runnables.isEmpty()) {
          LOG.error(String.format("%d tasks pending. Trying one last time.",
              runnables.size()));
        }

        // Wait a while for tasks to respond to being cancelled
        if (!executorService.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS)) {
          LOG.error("Executor Service did not terminate. " + executorService);
        }
      }

    } catch (InterruptedException e) { // If current thread is interrupted
      // Interrupt all currently executing tasks for the last time
      final List<Runnable> runnables = executorService.shutdownNow();
      if (!runnables.isEmpty()) {
        LOG.error(String.format("%d tasks still running. Thread interrupted!", runnables.size()));
      }
      Thread.currentThread().interrupt();
    }
  }

  public static ThreadFactory threadsNamed(final String nameFormat) {
    return new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
  }
}
