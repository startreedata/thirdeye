/*
 * Copyright 2024 StarTree Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandledRunnable  implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandledRunnable.class);

  private final Runnable target;

  public ExceptionHandledRunnable(final Runnable target) {
    this.target = target;
  }

  @Override
  public void run() {
    try {
      target.run();
    } catch (final Exception e) {
      LOG.error("Exception caught in runnable", e);
    }
  }
}
