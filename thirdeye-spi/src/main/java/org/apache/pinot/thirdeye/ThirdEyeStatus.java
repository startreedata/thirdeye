package org.apache.pinot.thirdeye;

public enum ThirdEyeStatus {

  ERR_DUPLICATE_NAME("Name must be unique!");

  final String message;

  ThirdEyeStatus(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
