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
package ai.startree.thirdeye.core;

public class ConfusionMatrix {
  private int truePositive = 0;
  private int falsePositive = 0;
  private int trueNegative = 0;
  private int falseNegative = 0;
  private int unclassified = 0;

  public int getTruePositive() {
    return truePositive;
  }

  public ConfusionMatrix addTruePositive(final int value) {
    this.truePositive += value;
    return this;
  }

  public int getFalsePositive() {
    return falsePositive;
  }

  public ConfusionMatrix addFalsePositive(final int value) {
    this.falsePositive += value;
    return this;
  }

  public int getTrueNegative() {
    return trueNegative;
  }

  public ConfusionMatrix addTrueNegative(final int value) {
    this.trueNegative += value;
    return this;
  }

  public int getFalseNegative() {
    return falseNegative;
  }

  public ConfusionMatrix addFalseNegative(final int value) {
    this.falseNegative += value;
    return this;
  }

  public int getUnclassified() {
    return unclassified;
  }

  public ConfusionMatrix addUnclassified(final int value) {
    this.unclassified += value;
    return this;
  }

  public void incTruePositive() {
    this.truePositive++;
  }

  public void incTrueNegative() {
    this.trueNegative++;
  }

  public void incFalsePositive() {
    this.falsePositive++;
  }

  public void incFalseNegative() {
    this.falseNegative++;
  }

  public void incUnclassified() {
    this.unclassified++;
  }

  public double getPrecision() {
    if(truePositive == 0) {
      return 0;
    } else {
      return truePositive / (double) (truePositive + falsePositive);
    }
  }

  public double getResponseRate() {
    if(unclassified == 0) {
      return 1;
    } else {
      return 1 - unclassified / (double) (truePositive + falsePositive + trueNegative + falseNegative + unclassified);
    }
  }
}
