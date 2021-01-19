import { isEmpty } from "lodash";
import {
    AlertEvaluationAnomalyPoint,
    AlertEvaluationTimeSeriesPoint,
} from "../../components/visualizations/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { formatLargeNumber } from "../number-util/number-util";

// Returns abbreviated string representation of number
// Equivalent to Number Util formatLargeNumber, but as required by visualizations
export const formatLargeNumberForVisualization = (
    num: number | { valueOf(): number }
): string => {
    if (!num || !num.valueOf) {
        return "";
    }

    if (typeof num === "number") {
        return formatLargeNumber(num);
    }

    return formatLargeNumber(num.valueOf());
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

    for (const index in detectionEvaluation.anomalies) {
        alertEvaluationAnomalyPoints.push({
            startTime: detectionEvaluation.anomalies[index].startTime,
            endTime: detectionEvaluation.anomalies[index].endTime,
            current: detectionEvaluation.anomalies[index].avgCurrentVal,
            baseline: detectionEvaluation.anomalies[index].avgBaselineVal,
        });
    }

    return alertEvaluationAnomalyPoints;
};

// Returns minimum timestamp from alert evaluation time series points
export const getAlertEvaluationTimeSeriesPointsMinTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    let minTimestamp = Number.MAX_VALUE;
    for (const alertEvaluationTimeSeriesPoint of alertEvaluationTimeSeriesPoints) {
        if (minTimestamp > alertEvaluationTimeSeriesPoint.timestamp) {
            minTimestamp = alertEvaluationTimeSeriesPoint.timestamp;
        }
    }

    return minTimestamp;
};

// Returns maximum timestamp from alert evaluation time series points
export const getAlertEvaluationTimeSeriesPointsMaxTimestamp = (
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[]
): number => {
    if (isEmpty(alertEvaluationTimeSeriesPoints)) {
        return 0;
    }

    let maxTimestamp = Number.MIN_VALUE;
    for (const alertEvaluationTimeSeriesPoint of alertEvaluationTimeSeriesPoints) {
        if (maxTimestamp < alertEvaluationTimeSeriesPoint.timestamp) {
            maxTimestamp = alertEvaluationTimeSeriesPoint.timestamp;
        }
    }

    return maxTimestamp;
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

// Returns maximum value from alert evaluation time series points
export const getAlertEvaluationAnomalyPointsMaxValue = (
    alertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[]
): number => {
    if (isEmpty(alertEvaluationAnomalyPoints)) {
        return 0;
    }

    let maxValue = Number.MIN_VALUE;
    for (const alertEvaluationAnomalyPoint of alertEvaluationAnomalyPoints) {
        // Current
        if (
            isFinite(alertEvaluationAnomalyPoint.current) &&
            maxValue < alertEvaluationAnomalyPoint.current
        ) {
            maxValue = alertEvaluationAnomalyPoint.current;
        }
    }

    return maxValue;
};
