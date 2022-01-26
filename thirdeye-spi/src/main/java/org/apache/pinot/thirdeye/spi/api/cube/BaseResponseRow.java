/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.api.cube;

/**
 * A POJO for front-end representation.
 */
public class BaseResponseRow {

  private double baselineValue;
  private double currentValue;
  private double sizeFactor;
  private double changePercentage;
  private double contributionChangePercentage;
  private double contributionToOverallChangePercentage;

  public double getBaselineValue() {
    return baselineValue;
  }

  public BaseResponseRow setBaselineValue(final double baselineValue) {
    this.baselineValue = baselineValue;
    return this;
  }

  public double getCurrentValue() {
    return currentValue;
  }

  public BaseResponseRow setCurrentValue(final double currentValue) {
    this.currentValue = currentValue;
    return this;
  }

  public double getSizeFactor() {
    return sizeFactor;
  }

  public BaseResponseRow setSizeFactor(final double sizeFactor) {
    this.sizeFactor = sizeFactor;
    return this;
  }

  public double getChangePercentage() {
    return changePercentage;
  }

  public BaseResponseRow setChangePercentage(final double changePercentage) {
    this.changePercentage = changePercentage;
    return this;
  }

  public double getContributionChangePercentage() {
    return contributionChangePercentage;
  }

  public BaseResponseRow setContributionChangePercentage(final double contributionChangePercentage) {
    this.contributionChangePercentage = contributionChangePercentage;
    return this;
  }

  public double getContributionToOverallChangePercentage() {
    return contributionToOverallChangePercentage;
  }

  public BaseResponseRow setContributionToOverallChangePercentage(
      final double contributionToOverallChangePercentage) {
    this.contributionToOverallChangePercentage = contributionToOverallChangePercentage;
    return this;
  }
}
