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
package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Map;
import java.util.Objects;

public class AnomalyLabelDTO {
  /**Name of the label - displayed in UI.*/
  private String name;
  /**Name of the postProcessor that generated this label.*/
  private String sourcePostProcessor;
  /**Name of the node that generated this label.*/
  private String sourceNodeName;
  /**Whether to ignore anomalies that have this label.*/
  private boolean ignore;
  /**Label specific metadata.*/
  private Map<String, Object> metadata;

  public String getName() {
    return name;
  }

  public AnomalyLabelDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getSourcePostProcessor() {
    return sourcePostProcessor;
  }

  public AnomalyLabelDTO setSourcePostProcessor(final String sourcePostProcessor) {
    this.sourcePostProcessor = sourcePostProcessor;
    return this;
  }

  public String getSourceNodeName() {
    return sourceNodeName;
  }

  public AnomalyLabelDTO setSourceNodeName(final String sourceNodeName) {
    this.sourceNodeName = sourceNodeName;
    return this;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public AnomalyLabelDTO setIgnore(final boolean ignore) {
    this.ignore = ignore;
    return this;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public AnomalyLabelDTO setMetadata(final Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AnomalyLabelDTO that = (AnomalyLabelDTO) o;
    return ignore == that.ignore && Objects.equals(name, that.name)
        && Objects.equals(sourcePostProcessor, that.sourcePostProcessor)
        && Objects.equals(sourceNodeName, that.sourceNodeName) && Objects.equals(
        metadata,
        that.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, sourcePostProcessor, sourceNodeName, ignore, metadata);
  }

  @Override
  public String toString() {
    return "AnomalyLabelDTO{" +
        "name='" + name + '\'' +
        ", sourcePostProcessor='" + sourcePostProcessor + '\'' +
        ", sourceNodeName='" + sourceNodeName + '\'' +
        ", ignore=" + ignore +
        ", metadata=" + metadata +
        '}';
  }
}
