/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.aspect;

import ai.startree.thirdeye.utils.TimeProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SystemTimeMockAspect {

  @Pointcut("call(public long java.lang.System.currentTimeMillis())")
  void currentTimeMillis() {
  }

  @Around("currentTimeMillis()")
  public Object aroundSystemcurrentTimeMillis(ProceedingJoinPoint pjp) throws Throwable {
    if (TimeProvider.instance().isTimedMocked()) {
      return TimeProvider.instance().currentTimeMillis();
    }
    return pjp.proceed();
  }
}
