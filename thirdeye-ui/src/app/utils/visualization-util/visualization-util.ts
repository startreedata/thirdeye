import { isEmpty } from "lodash";
import { AlertEvaluationTimeSeriesPoint } from "../../components/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { formatLargeNumber } from "../number-util/number-util";

// Returns abbreviated string representation of given large number
// Equivalent to Number Util formatLargeNumber, but as required by D3 for visualizations
export const formatLargeNumberForVisualization = (
    num: number | { valueOf(): number }
): string => {
    if (typeof num == "number") {
        return formatLargeNumber(num);
    }

    return formatLargeNumber(num.valueOf());
};

// Returns alert evaluation time series points from given alert evaluation
export const getAlertEvaluationTimeSeriesPoints = (
    alertEvaluation: AlertEvaluation
): AlertEvaluationTimeSeriesPoint[] => {
    const alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[] = [];

    if (!alertEvaluation || isEmpty(alertEvaluation.detectionEvaluations)) {
        return alertEvaluationTimeSeriesPoints;
    }

    // Gather only first available detection evaluation
    const detectionEvaluation = Object.values(
        alertEvaluation.detectionEvaluations
    )[0];

    if (
        !detectionEvaluation ||
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

// Returns minimum timestamp from given alert evaluation time series points
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

// Returns maximum timestamp from given alert evaluation time series points
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

// Returns maximum value from given alert evaluation time series points
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
