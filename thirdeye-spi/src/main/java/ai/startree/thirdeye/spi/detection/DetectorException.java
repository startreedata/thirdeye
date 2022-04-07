/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

/**
 * Base detector exception class.
 */
public class DetectorException extends Exception {

  public DetectorException(Throwable cause) {
    super(cause);
  }

  public DetectorException() {
    super();
  }

  public DetectorException(String message) {
    super(message);
  }

  public DetectorException(String message, Throwable cause) {
    super(message, cause);
  }
}
