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
package ai.startree.thirdeye.spi.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutorUtils.class);

  private ExecutorUtils() {
    // left blank
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
  public static void shutdownExecutionService(final ExecutorService executorService) {
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
    } catch (final InterruptedException e) { // If current thread is interrupted
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
