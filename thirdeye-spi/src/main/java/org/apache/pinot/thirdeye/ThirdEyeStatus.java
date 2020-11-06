package org.apache.pinot.thirdeye;

public enum ThirdEyeStatus {

  ERR_DUPLICATE_NAME("Name must be unique!"),
  ERR_MISSING_ID("ID is missing from the entity!"),
  ERR_OBJECT_DOES_NOT_EXIST("Object does not exist!"),
  ERR_UNKNOWN("Unknown Error!");

  final String message;

  ThirdEyeStatus(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
