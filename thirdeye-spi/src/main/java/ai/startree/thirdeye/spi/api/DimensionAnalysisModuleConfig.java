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

import java.util.List;

public class DimensionAnalysisModuleConfig {

  private List<String> includedDimension;
  private List<String> excludedDimension;
  private boolean manualOrder;
  private boolean oneSideError;
  private int summarySize;
  private int dimensionDepth;

  public List<String> getIncludedDimension() {
    return includedDimension;
  }

  public DimensionAnalysisModuleConfig setIncludedDimension(
      final List<String> includedDimension) {
    this.includedDimension = includedDimension;
    return this;
  }

  public List<String> getExcludedDimension() {
    return excludedDimension;
  }

  public DimensionAnalysisModuleConfig setExcludedDimension(
      final List<String> excludedDimension) {
    this.excludedDimension = excludedDimension;
    return this;
  }

  public boolean isManualOrder() {
    return manualOrder;
  }

  public DimensionAnalysisModuleConfig setManualOrder(final boolean manualOrder) {
    this.manualOrder = manualOrder;
    return this;
  }

  public boolean isOneSideError() {
    return oneSideError;
  }

  public DimensionAnalysisModuleConfig setOneSideError(final boolean oneSideError) {
    this.oneSideError = oneSideError;
    return this;
  }

  public int getSummarySize() {
    return summarySize;
  }

  public DimensionAnalysisModuleConfig setSummarySize(final int summarySize) {
    this.summarySize = summarySize;
    return this;
  }

  public int getDimensionDepth() {
    return dimensionDepth;
  }

  public DimensionAnalysisModuleConfig setDimensionDepth(final int dimensionDepth) {
    this.dimensionDepth = dimensionDepth;
    return this;
  }
}
