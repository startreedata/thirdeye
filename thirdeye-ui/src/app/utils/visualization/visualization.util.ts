import { ScaleTime } from "d3-scale";
import { isEmpty } from "lodash";
import { Interval } from "luxon";
import {
    AlertEvaluationAnomalyPoint,
    AlertEvaluationTimeSeriesPoint,
} from "../../components/visualizations/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import {
    formatDate,
    formatMonthOfYear,
    formatTime,
    formatYear,
} from "../date-time/date-time.util";
import { formatLargeNumber } from "../number/number.util";

export const SEPARATOR_DATE_TIME = "@";

// Returns abbreviated string representation of number
// Equivalent to Number Util formatLargeNumber, but as required by D3
export const formatLargeNumberForVisualization = (
    num: number | { valueOf(): number }
): string => {
    if (!num) {
        return "";
    }

    if (typeof num === "number") {
        return formatLargeNumber(num);
    }

    return formatLargeNumber(num.valueOf());
};

// Returns formatted string representation of date based on scale domain interval
// For example:
// Time interval > 2 years - YYYY
// 2 years >= Time interval > 2 months - MMM YYYY
// 2 months >= Time interval > 2 days - MMM DD, YYYY
// 2 days >= Time interval - MMM DD, YY SEPARATOR_DATE_TIME HH:MM AM/PM
export const formatDateTimeForAxis = (
    date: number | { valueOf(): number },
    scale: ScaleTime<number, number>
): string => {
    if (!date || !scale) {
        return "";
    }

    // Capture date
    let targetDate;
    if (typeof date === "number") {
        targetDate = date;
    } else {
        targetDate = date.valueOf();
    }

    // Determine scale domain interval duration
    const duration = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    ).toDuration();

    if (duration.as("years") > 2) {
        // YYYY
        return formatYear(targetDate);
    }

    if (duration.as("months") > 2) {
        // MMM YYYY
        return formatMonthOfYear(targetDate);
    }

    if (duration.as("days") > 2) {
        // MMM DD, YYYY
        return formatDate(targetDate);
    }

    // MMM DD, YY SEPARATOR_DATE_TIME HH:MM AM/PM
    return (
        formatDate(targetDate) + SEPARATOR_DATE_TIME + formatTime(targetDate)
    );
};

// Returns equally spaced time tick values for scale domain interval
export const getTimeTickValuesForAxis = (
    numTicks: number,
    scale: ScaleTime<number, number>
): number[] => {
    if (!scale) {
        return [];
    }

    numTicks = numTicks || 8; // Default to 8 ticks
    if (numTicks < 3) {
        // Just the scale domain interval start and end time
        return [scale.domain()[0].getTime(), scale.domain()[1].getTime()];
    }

    // Determine scale domain interval
    const interval = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    );
    const splitIntervals = interval.divideEqually(numTicks - 1); // Account for scale domain start and end time as two time tick values

    const timeTickValues: number[] = [];
    splitIntervals.forEach((splitInterval, index) => {
        if (index === splitIntervals.length - 1) {
            // Same as scale domain end time
            return;
        }

        timeTickValues.push(splitInterval.end.toMillis());
    });

    return [
        scale.domain()[0].getTime(),
        ...timeTickValues,
        scale.domain()[1].getTime(),
    ];
};

// Returns alert evaluation time series points from alert evaluation
export const getAlertEvaluationTimeSeriesPoints = (
    alertEvaluation: AlertEvaluation
): AlertEvaluationTimeSeriesPoint[] => {
    const alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[] = [];

    if (!alertEvaluation || isEmpty(alertEvaluation.detectionEvaluations)) {
        return alertEvaluationTimeSeriesPoints;
    }

    // Gather only first detection evaluation
    const detectionEvaluation = Object.values(
        alertEvaluation.detectionEvaluations
    )[0];

    if (
        isEmpty(detectionEvaluation.data) ||
        isEmpty(detectionEvaluation.data.timestamp)
    ) {
        return alertEvaluationTimeSeriesPoints;
    }

    for (const index in detectionEvaluation.data.timestamp) {
        alertEvaluationTimeSeriesPoints.push({
            timestamp: detectionEvaluation.data.timestamp[index],
            upperBound: detectionEvaluation.data.upperBound[index],
            lowerBound: detectionEvaluation.data.lowerBound[index],
            current: detectionEvaluation.data.current[index],
            expected: detectionEvaluation.data.expected[index],
        });
    }

    return alertEvaluationTimeSeriesPoints;
};

// Returns alert evaluation anomaly points from given alert evaluation
export const getAlertEvaluationAnomalyPoints = (
    alertEvaluation: AlertEvaluation
): AlertEvaluationAnomalyPoint[] => {
    const alertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[] = [];

    if (!alertEvaluation || isEmpty(alertEvaluation.detectionEvaluations)) {
        return alertEvaluationAnomalyPoints;
    }

    // Gather only first detection evaluation
    const detectionEvaluation = Object.values(
        alertEvaluation.detectionEvaluations
    )[0];

    if (isEmpty(detectionEvaluation.anomalies)) {
        return alertEvaluationAnomalyPoints;
    }

    for (const anomaly of detectionEvaluation.anomalies) {
        alertEvaluationAnomalyPoints.push({
            startTime: anomaly.startTime,
            endTime: anomaly.endTime,
            current: anomaly.avgCurrentVal,
            baseline: anomaly.avgBaselineVal,
        });
    }

    return alertEvaluationAnomalyPoints;
};

// Returns minimum timestamp from alert evaluation time series points
export const getAlertEvaluationTimeSeriesPointsMinTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    return (
        (alertEvaluationTimeSeriesPoints &&
            alertEvaluationTimeSeriesPoints[0] &&
            alertEvaluationTimeSeriesPoints[0].timestamp) ||
        0
    );
};

// Returns maximum timestamp from alert evaluation time series points
export const getAlertEvaluationTimeSeriesPointsMaxTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    return (
        (alertEvaluationTimeSeriesPoints &&
            alertEvaluationTimeSeriesPoints[
                alertEvaluationTimeSeriesPoints.length - 1
            ] &&
            alertEvaluationTimeSeriesPoints[
                alertEvaluationTimeSeriesPoints.length - 1
            ].timestamp) ||
        0
    );
};

// Returns maximum value from alert evaluation time series points
export const getAlertEvaluationTimeSeriesPointsMaxValue = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    let maxValue = Number.MIN_VALUE;
    for (const alertEvaluationTimeSeriesPoint of alertEvaluationTimeSeriesPoints) {
        // Upper bound
        if (
            isFinite(alertEvaluationTimeSeriesPoint.upperBound) &&
            maxValue < alertEvaluationTimeSeriesPoint.upperBound
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.upperBound;
        }

        // Lower bound
        if (
            isFinite(alertEvaluationTimeSeriesPoint.lowerBound) &&
            maxValue < alertEvaluationTimeSeriesPoint.lowerBound
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.lowerBound;
        }

        // Current
        if (
            isFinite(alertEvaluationTimeSeriesPoint.current) &&
            maxValue < alertEvaluationTimeSeriesPoint.current
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.current;
        }

        // Baseline
        if (
            isFinite(alertEvaluationTimeSeriesPoint.expected) &&
            maxValue < alertEvaluationTimeSeriesPoint.expected
        ) {
            maxValue = alertEvaluationTimeSeriesPoint.expected;
        }
    }

    return maxValue;
};
