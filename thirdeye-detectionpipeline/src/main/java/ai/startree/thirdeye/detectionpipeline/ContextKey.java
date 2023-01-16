/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline;

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
