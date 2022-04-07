/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
