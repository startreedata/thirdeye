///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import bounds from "binary-search-bounds";
import { ScaleTime } from "d3-scale";
import { isEmpty, isNil } from "lodash";
import { Interval } from "luxon";
import { AlertEvaluationTimeSeriesPoint } from "../../components/visualizations/alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import {
    formatDateV1,
    formatLargeNumberV1,
    formatMonthOfYearV1,
    formatTimeV1,
    formatYearV1,
} from "../../platform/utils";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export const SEPARATOR_DATE_TIME = "@";
export const NUM_TICKS = 8;

// Returns abbreviated string representation of number
// Equivalent to Number Util formatLargeNumber, but as required by D3
export const formatLargeNumberForVisualization = (
    num: number | { valueOf(): number }
): string => {
    if (isNil(num)) {
        return "";
    }

    let targetNum;
    if (typeof num === "number") {
        targetNum = num;
    } else if (num.valueOf && typeof num.valueOf() === "number") {
        targetNum = num.valueOf();
    }

    if (isNil(targetNum)) {
        return "";
    }

    return formatLargeNumberV1(targetNum);
};

// Returns formatted string representation of date based on scale domain interval
// For example:
// Interval > 2 years -> YYYY
// 2 years >= interval > 2 months -> MMM YYYY
// 2 months >= interval > 2 days -> MMM DD, YYYY
// 2 days >= interval -> MMM DD, YY SEPARATOR_DATE_TIME HH:MM AM/PM
export const formatDateTimeForTimeAxis = (
    date: number | { valueOf(): number },
    scale: ScaleTime<number, number>
): string => {
    if (isNil(date) || !scale) {
        return "";
    }

    let targetDate;
    if (typeof date === "number") {
        targetDate = date;
    } else if (date.valueOf && typeof date.valueOf() === "number") {
        targetDate = date.valueOf();
    }

    if (isNil(targetDate)) {
        return "";
    }

    // Determine scale domain interval duration
    const duration = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    ).toDuration();

    if (duration.as("years") > 2) {
        // YYYY
        return formatYearV1(targetDate);
    }

    if (duration.as("months") > 2) {
        // MMM YYYY
        return formatMonthOfYearV1(targetDate);
    }

    if (duration.as("days") > 2) {
        // MMM DD, YYYY
        return formatDateV1(targetDate);
    }

    // MMM DD, YYYY SEPARATOR_DATE_TIME HH:MM AM/PM
    return (
        formatDateV1(targetDate) +
        SEPARATOR_DATE_TIME +
        formatTimeV1(targetDate)
    );
};

// Returns equally spaced tick values for scale domain interval
export const getTickValuesForTimeAxis = (
    scale: ScaleTime<number, number>,
    numTicks?: number
): number[] => {
    if (!scale) {
        return [];
    }

    if (isNil(numTicks)) {
        numTicks = NUM_TICKS;
    }

    if (numTicks < 3) {
        // Just the scale domain start and end time
        return [scale.domain()[0].getTime(), scale.domain()[1].getTime()];
    }

    // Determine scale domain interval
    const interval = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    );
    const splitIntervals = interval.divideEqually(numTicks - 1); // Account for scale domain start and end time as two tick values
    if (isEmpty(splitIntervals)) {
        // Just the scale domain start and end time
        return [scale.domain()[0].getTime(), scale.domain()[1].getTime()];
    }

    const timeTickValues = [];
    for (let index = 0; index < splitIntervals.length - 1; index++) {
        // Ignore last interval, same as scale domain end time
        timeTickValues.push(splitIntervals[index].end.toMillis());
    }

    return [
        scale.domain()[0].getTime(),
        ...timeTickValues,
        scale.domain()[1].getTime(),
    ];
};

export const getAlertEvaluationTimeSeriesPoints = (
    alertEvaluation: AlertEvaluation
): AlertEvaluationTimeSeriesPoint[] => {
    if (!alertEvaluation || isEmpty(alertEvaluation.detectionEvaluations)) {
        return [];
    }

    // Gather only the first detection evaluation
    const detectionEvaluation = Object.values(
        alertEvaluation.detectionEvaluations
    )[0];
    if (
        isEmpty(detectionEvaluation.data) ||
        isEmpty(detectionEvaluation.data.timestamp)
    ) {
        return [];
    }

    const alertEvaluationTimeSeriesPoints = [];
    for (
        let index = 0;
        index < detectionEvaluation.data.timestamp.length;
        index++
    ) {
        alertEvaluationTimeSeriesPoints.push({
            timestamp: detectionEvaluation.data.timestamp[index],
            current: detectionEvaluation.data.current[index],
            expected: detectionEvaluation.data.expected[index],
            upperBound: detectionEvaluation.data.upperBound[index],
            lowerBound: detectionEvaluation.data.lowerBound[index],
        });
    }

    return alertEvaluationTimeSeriesPoints;
};

export const getAlertEvaluationAnomalies = (
    alertEvaluation: AlertEvaluation
): Anomaly[] => {
    if (!alertEvaluation || isEmpty(alertEvaluation.detectionEvaluations)) {
        return [];
    }

    // Gather only the first detection evaluation
    const detectionEvaluation = Object.values(
        alertEvaluation.detectionEvaluations
    )[0];

    return detectionEvaluation.anomalies || [];
};

export const getAlertEvaluationTimeSeriesPointsMinTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    // Alert evaluation time series points assumed to be sorted by timestamp
    return alertEvaluationTimeSeriesPoints[0].timestamp;
};

export const getAlertEvaluationTimeSeriesPointsMaxTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    // Alert evaluation time series points assumed to be sorted by timestamp
    return alertEvaluationTimeSeriesPoints[
        alertEvaluationTimeSeriesPoints.length - 1
    ].timestamp;
};

export const getAlertEvaluationTimeSeriesPointsMaxValue = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    let maxValue = Number.MIN_VALUE;
    for (const alertEvaluationTimeSeriesPoint of alertEvaluationTimeSeriesPoints) {
        // Current
        if (
            Number.isFinite(alertEvaluationTimeSeriesPoint.current) &&
            maxValue < alertEvaluationTimeSeriesPoint.current
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.current;
        }

        // Baseline
        if (
            Number.isFinite(alertEvaluationTimeSeriesPoint.expected) &&
            maxValue < alertEvaluationTimeSeriesPoint.expected
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.expected;
        }

        // Upper bound
        if (
            Number.isFinite(alertEvaluationTimeSeriesPoint.upperBound) &&
            maxValue < alertEvaluationTimeSeriesPoint.upperBound
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.upperBound;
        }

        // Lower bound
        if (
            Number.isFinite(alertEvaluationTimeSeriesPoint.lowerBound) &&
            maxValue < alertEvaluationTimeSeriesPoint.lowerBound
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.lowerBound;
        }
    }

    return maxValue;
};

export const filterAlertEvaluationTimeSeriesPointsByTime = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[],
    startTime: number,
    endTime: number
): AlertEvaluationTimeSeriesPoint[] => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return [];
    }

    if (isNil(startTime) || isNil(endTime)) {
        return alertEvaluationTimeSeriesPoints;
    }

    // Alert evaluation time series points assumed to be sorted by timestamp
    // Search first alert evaluation time series point with timestamp greater than or equal to start
    // time
    const startIndex = bounds.ge(
        alertEvaluationTimeSeriesPoints,
        { timestamp: startTime } as AlertEvaluationTimeSeriesPoint,
        alertEvaluationTimeSeriesPointsComparator
    );
    if (startIndex === alertEvaluationTimeSeriesPoints.length) {
        // Not found
        return [];
    }

    // Search first alert evaluation time series point with timestamp less than or equal to end time
    const endIndex = bounds.le(
        alertEvaluationTimeSeriesPoints,
        { timestamp: endTime } as AlertEvaluationTimeSeriesPoint,
        alertEvaluationTimeSeriesPointsComparator
    );

    return alertEvaluationTimeSeriesPoints.slice(startIndex, endIndex + 1);
};

export const getAlertEvaluationTimeSeriesPointAtTime = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[],
    time: number
): AlertEvaluationTimeSeriesPoint | null => {
    if (isEmpty(alertEvaluationTimeSeriesPoints) || isNil(time)) {
        return null;
    }

    // Search first alert evaluation time series point with timestamp closest or equal to time
    const index = bounds.le(
        alertEvaluationTimeSeriesPoints,
        { timestamp: time } as AlertEvaluationTimeSeriesPoint,
        alertEvaluationTimeSeriesPointsComparator
    );
    if (index === -1) {
        // Not found
        return null;
    }

    return alertEvaluationTimeSeriesPoints[index];
};

const alertEvaluationTimeSeriesPointsComparator = (
    alertEvaluationTimeSeriesPoint1: AlertEvaluationTimeSeriesPoint,
    alertEvaluationTimeSeriesPoint2: AlertEvaluationTimeSeriesPoint
): number => {
    return (
        alertEvaluationTimeSeriesPoint1.timestamp -
        alertEvaluationTimeSeriesPoint2.timestamp
    );
};
