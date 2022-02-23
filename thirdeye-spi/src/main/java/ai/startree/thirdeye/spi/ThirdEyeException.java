/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi;

public class ThirdEyeException extends RuntimeException {

  private final ThirdEyeStatus status;

  public ThirdEyeException(final ThirdEyeStatus status, Object... args) {
    super(getMsg(status, args));
    this.status = status;
  }

  public ThirdEyeException(final Throwable cause, final ThirdEyeStatus status, Object... args) {
    super(getMsg(status, args), cause);
    this.status = status;
  }

  private static String getMsg(final ThirdEyeStatus status, final Object[] args) {
    return String.format(status.getMessage(), args);
  }

  public ThirdEyeStatus getStatus() {
    return status;
  }
}
