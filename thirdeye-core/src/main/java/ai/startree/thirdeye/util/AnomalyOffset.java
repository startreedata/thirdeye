/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import org.joda.time.Period;

public class AnomalyOffset {

  Period preOffsetPeriod;
  Period postOffsetPeriod;

  public AnomalyOffset(Period preOffsetPeriod, Period postOffsetPeriod) {
    this.preOffsetPeriod = preOffsetPeriod;
    this.postOffsetPeriod = postOffsetPeriod;
  }

  public Period getPreOffsetPeriod() {
    return preOffsetPeriod;
  }

  public void setPreOffsetPeriod(Period preOffsetPeriod) {
    this.preOffsetPeriod = preOffsetPeriod;
  }

  public Period getPostOffsetPeriod() {
    return postOffsetPeriod;
  }

  public void setPostOffsetPeriod(Period postOffsetPeriod) {
    this.postOffsetPeriod = postOffsetPeriod;
  }
}
