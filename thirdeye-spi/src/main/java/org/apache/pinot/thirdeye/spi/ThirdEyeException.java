package org.apache.pinot.thirdeye.spi;

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
