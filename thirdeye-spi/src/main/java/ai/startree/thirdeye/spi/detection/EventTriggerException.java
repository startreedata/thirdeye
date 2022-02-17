/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

/**
 * Base detector exception class.
 */
public class EventTriggerException extends Exception {

  public EventTriggerException(Throwable cause) {
    super(cause);
  }

  public EventTriggerException() {
    super();
  }

  public EventTriggerException(String message) {
    super(message);
  }

  public EventTriggerException(String message, Throwable cause) {
    super(message, cause);
  }
}
