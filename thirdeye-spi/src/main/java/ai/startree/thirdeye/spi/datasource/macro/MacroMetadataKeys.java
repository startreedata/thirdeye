/*
 * Copyright 2022 StarTree Inc
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
