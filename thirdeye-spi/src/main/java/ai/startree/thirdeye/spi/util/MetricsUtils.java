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
import java.util.Optional;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utilities for micrometer metrics.
 */
public class MetricsUtils {

  public final static String NAMESPACE_TAG = "thirdeye_workspace";
  public final static String NULL_NAMESPACE_TAG_VALUE = "__null__";

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

  // if the callable does not throw, record time in the successTimer. 
  // Else, record time in the exceptionTimer. 
  public static void record(Runnable fun, final Timer successTimer, final Timer exceptionTimer)
      throws Exception {
    final Timer.Sample sample = Timer.start(Metrics.globalRegistry);
    try {
      fun.run();
      sample.stop(successTimer);
    } catch (Exception e) {
      sample.stop(exceptionTimer);
      throw e;
    }
  }

  public static @NonNull String namespaceTagValueOf(@Nullable String namespace) {
    return Optional.ofNullable(namespace).orElse(NULL_NAMESPACE_TAG_VALUE);
  }
}
