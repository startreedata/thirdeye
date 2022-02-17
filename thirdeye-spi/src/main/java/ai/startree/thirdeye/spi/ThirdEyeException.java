/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi;

public class ThirdEyeException extends RuntimeException {

  private final ThirdEyeStatus status;

  public ThirdEyeException(final ThirdEyeStatus status, Object... args) {
    super(String.format(status.getMessage(), args));
    this.status = status;
  }

  public ThirdEyeStatus getStatus() {
    return status;
  }
}
