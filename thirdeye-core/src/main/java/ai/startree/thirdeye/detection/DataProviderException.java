/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

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
