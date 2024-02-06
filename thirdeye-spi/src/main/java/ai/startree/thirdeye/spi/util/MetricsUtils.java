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

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.Callable;

/**
 * Utilities for micrometer metrics.
 */
public class MetricsUtils {


  // if the callable does not throw, record time in the successTimer. 
  // Else, record time in the exceptionTimer. 
  public static <E> E record(Callable<E> fun, final Timer successTimer, final Timer exceptionTimer)
      throws Exception {
    final Timer.Sample sample = Timer.start(Metrics.globalRegistry);
    try {
      E res = fun.call();
      sample.stop(successTimer);
      return res;
    } catch (Exception e) {
      sample.stop(exceptionTimer);
      throw e;
    }
  }
}
