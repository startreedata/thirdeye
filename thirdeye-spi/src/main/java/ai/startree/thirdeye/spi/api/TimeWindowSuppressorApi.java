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
package ai.startree.thirdeye.spi.api;

public class TimeWindowSuppressorApi {

    private Long windowStartTime;
    private Long windowEndTime;
    private Boolean isThresholdApplied;
    private Double expectedChange;
    private Double acceptableDeviation;

    public Long getWindowStartTime() {
        return windowStartTime;
    }

    public TimeWindowSuppressorApi setWindowStartTime(Long windowStartTime) {
        this.windowStartTime = windowStartTime;
        return this;
    }

    public Long getWindowEndTime() {
        return windowEndTime;
    }

    public TimeWindowSuppressorApi setWindowEndTime(Long windowEndTime) {
        this.windowEndTime = windowEndTime;
        return this;
    }

    public Boolean getThresholdApplied() {
        return isThresholdApplied;
    }

    public TimeWindowSuppressorApi setThresholdApplied(Boolean thresholdApplied) {
        isThresholdApplied = thresholdApplied;
        return this;
    }

    public Double getExpectedChange() {
        return expectedChange;
    }

    public TimeWindowSuppressorApi setExpectedChange(Double expectedChange) {
        this.expectedChange = expectedChange;
        return this;
    }

    public Double getAcceptableDeviation() {
        return acceptableDeviation;
    }

    public TimeWindowSuppressorApi setAcceptableDeviation(Double acceptableDeviation) {
        this.acceptableDeviation = acceptableDeviation;
        return this;
    }
}
