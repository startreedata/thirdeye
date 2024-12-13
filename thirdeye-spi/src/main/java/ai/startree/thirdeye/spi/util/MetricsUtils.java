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

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for micrometer metrics.
 */
public class MetricsUtils {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsUtils.class);
  
  // the number 3 is not optimized - we just know 1 is not optimal - some queries performed by scheduled suppliers can take time   
  private static final ScheduledExecutorService SCHEDULED_SUPPLIERS_EXECUTOR_SERVICE = Executors
      .newScheduledThreadPool(3, new ThreadFactoryBuilder()
          .setNameFormat("scheduled-refresh-metrics-%d")
          .build());

  static {
    ExecutorServiceMetrics.monitor(Metrics.globalRegistry, SCHEDULED_SUPPLIERS_EXECUTOR_SERVICE, "scheduled-refresh-metrics");
  }
  
  public final static String NAMESPACE_TAG = "thirdeye_workspace";
  public final static String NULL_NAMESPACE_TAG_VALUE = "__null__";

  // if the callable does not throw, record time in the successTimer. 
  // Else, record time in the exceptionTimer. 
  public static <E> E record(final Callable<E> fun, final Timer successTimer, final Timer exceptionTimer)
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
  public static void record(final Runnable fun, final Timer successTimer, final Timer exceptionTimer)
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
  
  public static <T extends @Nullable Object> Supplier<T> scheduledRefreshSupplier(
      final @NonNull Supplier<T> delegate, final @NonNull Duration period) {
    final AtomicReference<T> value = new AtomicReference<>();
    final Runnable toSchedule = () -> {
      // catch all exceptions to prevent unscheduling 
      try {
        final T currentValue = value.get();
        final T newValue = delegate.get();
        value.compareAndSet(currentValue, newValue);
      } catch (Exception e) {
        LOG.error("Failed to execute scheduled supplier.", e);
      }
    };
    // add random delay to spread the load of the scheduled suppliers
    final long periodInMillis = period.toMillis();
    final long randomDelay = ThreadLocalRandom.current().nextLong(0, periodInMillis);
    SCHEDULED_SUPPLIERS_EXECUTOR_SERVICE.scheduleAtFixedRate(toSchedule,
        randomDelay,
        periodInMillis, 
        TimeUnit.MILLISECONDS);
    
    return () -> {
      if (value.get() == null) {
          toSchedule.run();
      }
      return value.get();
    };
  }
}
