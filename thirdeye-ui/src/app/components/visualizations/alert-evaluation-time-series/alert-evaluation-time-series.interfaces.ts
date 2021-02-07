import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";

export interface AlertEvaluationTimeSeriesProps {
    alertEvaluation: AlertEvaluation | null;
}

export interface AlertEvaluationTimeSeriesInternalProps {
    width: number;
    height: number;
    alertEvaluation: AlertEvaluation | null;
}

export enum AlertEvaluationTimeSeriesPlot {
    CURRENT = "CURRENT",
    BASELINE = "BASELINE",
    UPPER_AND_LOWER_BOUND = "UPPER_AND_LOWER_BOUND",
    ANOMALIES = "ANOMALIES",
}

export interface AlertEvaluationTimeSeriesPoint {
    timestamp: number;
    current: number;
    expected: number;
    upperBound: number;
    lowerBound: number;
}

export interface AlertEvaluationAnomalyPoint {
    startTime: number;
    endTime: number;
    current: number;
    baseline: number;
}
