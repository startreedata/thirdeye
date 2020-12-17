package org.apache.pinot.thirdeye.detection;

public class DataProviderException extends RuntimeException {

  public DataProviderException() {
    super();
  }

  public DataProviderException(final String msg, final Exception e) {
    super(msg, e);
  }

  public DataProviderException(final Throwable e) {
    super(e);
  }
}
