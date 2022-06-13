/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert;

import java.util.Objects;
import java.util.StringJoiner;

public class ContextKey {

  final String nodeName;
  final String key;

  public ContextKey(final String nodeName, final String key) {
    this.nodeName = nodeName;
    this.key = key;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getKey() {
    return key;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ContextKey that = (ContextKey) o;
    return Objects.equals(nodeName, that.nodeName) &&
        Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeName, key);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ContextKey.class.getSimpleName() + "[", "]")
        .add("nodeName='" + nodeName + "'")
        .add("key='" + key + "'")
        .toString();
  }
}
