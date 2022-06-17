/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.macro;

/**
 * String keys to use by a macro function when it writes to the properties map.
 * */
public enum MacroMetadataKeys {
  MIN_TIME_MILLIS("metadata.minTimeMillis"),
  MAX_TIME_MILLIS("metadata.maxTimeMillis"),
  GRANULARITY("metadata.granularity");

  private final String key;

  MacroMetadataKeys(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return key;
  }
}
