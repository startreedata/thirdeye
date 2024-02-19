package ai.startree.thirdeye;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimedInterceptor implements MethodInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(TimedInterceptor.class);
  public static final List<Class<? extends Annotation>> HTTP_METHOD_CLASSES = List.of(GET.class,
      POST.class, PUT.class, DELETE.class, PATCH.class,
      OPTIONS.class, HEAD.class);

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    final Method method = invocation.getMethod();
    final Timed annotation = method.getAnnotation(Timed.class);
    if (annotation != null) {
      final String name = annotation.value().isEmpty() ? defaultName(method) : annotation.value();
      if (annotation.longTask()) {
        throw new UnsupportedOperationException(
            "LongTaskTimer not implemented yet in Timed annotation.");
      }
      final Timer timer = Timer.builder(name)
          .tags(annotation.extraTags())
          .description(annotation.description())
          .publishPercentiles(annotation.percentiles())
          .publishPercentileHistogram(annotation.histogram())
          .register(Metrics.globalRegistry);
      final Sample sample = Timer.start(Metrics.globalRegistry);
      try {
        return invocation.proceed();
      } finally {
        sample.stop(timer);
      }
    }

    // fallback case that should not happen if TimerInterceptor is used correctly
    // TimerInterceptor should be added with eg:
    // bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
    // so the match only happens on methods annotated with @Timed.
    LOG.error(
        "The TimedInterceptor was called on a method without a Timed annotation. This should not happen. Please reach out to support.");
    return invocation.proceed();
  }

  @NonNull
  private static String defaultName(final Method method) {
    
    return "thirdeye_method_" + method.getDeclaringClass().getSimpleName() + "_" + method.getName();
  }
}
