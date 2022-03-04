package ai.startree.thirdeye.aspect;

import ai.startree.thirdeye.utils.TimeProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class QuartzRandomizedIdleWaitTimeMockAspect {

  @Pointcut("call(private long org.quartz.core.QuartzSchedulerThread.getRandomizedIdleWaitTime())")
  void smallIdleWaitTime() {
  }

  @Around("smallIdleWaitTime()")
  public Object aroundRandomizedIdleWaitTime(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      // time is controlled manually - make quartz idle time small for test speed
      return 1000;
    }
    return pjp.proceed();
  }
}
