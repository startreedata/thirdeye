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
package ai.startree.thirdeye;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndToEndTest {

  private static final Logger log = LoggerFactory.getLogger(EndToEndTest.class);

  private final ExecutorService coordinatorExecutor;
  private final ExecutorService workerExecutor;

  public EndToEndTest() {
    coordinatorExecutor = Executors.newSingleThreadExecutor();
    workerExecutor = Executors.newSingleThreadExecutor();
  }

  public static void main(String[] args) {
    EndToEndTest instance = new EndToEndTest();
    instance.init();

    instance.awaitTermination(5);
  }

  private void awaitTermination(final int timeoutSeconds) {
    try {
      coordinatorExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
      workerExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Interrupted!", e);
    }
  }

  private void init() {
    coordinatorExecutor.execute(catchAll(() -> ThirdEyeServer
        .main(new String[]{"server", "thirdeye-integration-tests/config/server.yaml"})));

//    workerExecutor.execute(catchAll(() -> ThirdEyeWorker
//        .main(new String[]{"server", "thirdeye-integration-tests/config/server.yaml"})));
  }

  private Runnable catchAll(RunnableWithException r) {
    return () -> {
      try {
        r.run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  @FunctionalInterface
  public interface RunnableWithException {
    void run() throws Exception;
  }
}
