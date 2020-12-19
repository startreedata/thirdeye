import { bisector, format, timeFormat } from "d3";
import { isEmpty } from "lodash";
import { AlertEvaluationTimeSeriesPoint } from "../../components/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { TimeSeriesAnomaly } from "../../components/anomaly-chart/anomaly-chart.interface";
import {
    Margin,
    TimeSeriesProps,
} from "../../components/timeseries-chart/timeseries-chart.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { formatMonthDayDate } from "../date-time-util/date-time-util";
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

// Returns formatted string representation of month and day part of given date
// Equivalent to Date Time Util formatMonthDayDate, but as required by D3 for visualizations
export const formatMonthDayDateForVisualization = (
    date: Date | number | { valueOf(): number }
): string => {
    if (date instanceof Date) {
        return formatMonthDayDate(date.getTime());
    }

    if (typeof date === "number") {
        return formatMonthDayDate(date);
    }

    return formatMonthDayDate(date.valueOf());
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

export const CHART_SEPRATION_HEIGHT = 30;

export const getMargins = ({ showLegend }: { showLegend: boolean }): Margin => {
    return {
        left: 40,
        top: 10,
        bottom: 30,
        right: showLegend ? 150 : 20,
    };
};

export const getTimeSeriesFromAlertEvalution = (
    data: AlertEvaluation
): TimeSeriesProps[] => {
    if (isEmpty(data)) {
        return [];
    }
    const detectionKeys = Object.keys(data.detectionEvaluations);

    const lineChartData = data.detectionEvaluations[detectionKeys[0]].data;

    return lineChartData.timestamp.map((time, idx) => ({
        timestamp: new Date(time),
        current: lineChartData.current[idx],
        expacted: +lineChartData.expected[idx],
        lowerBound: +lineChartData.lowerBound[idx],
        upperBound: +lineChartData.upperBound[idx],
    }));
};

export const getAnomaliesFromAlertEvalution = (
    data: AlertEvaluation
): Anomaly[] => {
    if (isEmpty(data)) {
        return [];
    }
    const detectionKeys = Object.keys(data.detectionEvaluations);

    return data.detectionEvaluations[detectionKeys[0]].anomalies;
};

// accessors
export const getDate = (d: TimeSeriesProps | TimeSeriesAnomaly): Date =>
    d.timestamp;
export const getValue = (d: TimeSeriesProps | TimeSeriesAnomaly): number =>
    isNaN(d.current) ? 0 : d.current;
export const getBaseline = (d: TimeSeriesProps): number =>
    isNaN(d.expacted) ? 0 : d.expacted;
export const getLowerBound = (d: TimeSeriesProps): number =>
    isNaN(d.lowerBound) ? d.current : d.lowerBound;
export const getUpperBound = (d: TimeSeriesProps): number =>
    isNaN(d.current) ? 0 : d.current;

// Utils functions for axis and scaling
export const formatDateSort = timeFormat("%b %d");
export const formatDateDetailed = timeFormat("%b %d, %H:%M %p");
export const formatDate = (
    date: Date | number | { valueOf(): number },
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    _i: number
): string => formatDateSort(date as Date);

export const formatValue = (d: number | { valueOf(): number }): string =>
    format("~s")(d);
export const bisectDate = bisector<TimeSeriesProps, Date>(
    (d) => new Date(d.timestamp)
).left;
