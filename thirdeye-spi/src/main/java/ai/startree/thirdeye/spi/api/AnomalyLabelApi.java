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
package ai.startree.thirdeye.spi.api;

import java.util.Map;

public class AnomalyLabelApi {
  private String name;
  private String sourcePostProcessor;
  private String sourceNodeName;
  private boolean ignore;
  private Map<String, Object> metadata;

  public String getName() {
    return name;
  }

  public AnomalyLabelApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getSourcePostProcessor() {
    return sourcePostProcessor;
  }

  public AnomalyLabelApi setSourcePostProcessor(final String sourcePostProcessor) {
    this.sourcePostProcessor = sourcePostProcessor;
    return this;
  }

  public String getSourceNodeName() {
    return sourceNodeName;
  }

  public AnomalyLabelApi setSourceNodeName(final String sourceNodeName) {
    this.sourceNodeName = sourceNodeName;
    return this;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public AnomalyLabelApi setIgnore(final boolean ignore) {
    this.ignore = ignore;
    return this;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public AnomalyLabelApi setMetadata(final Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }
}
