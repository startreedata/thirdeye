/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.aspect;

import ai.startree.thirdeye.utils.TimeProvider;
import java.util.Date;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DateMockAspect {

  @Pointcut("call(java.util.Date.new())")
  void currentDate() {
  }

  @Around("currentDate()")
  public Object aroundCurrentDate(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      return new Date(TimeProvider.instance().currentTimeMillis());
    }
    return pjp.proceed();
  }
}
