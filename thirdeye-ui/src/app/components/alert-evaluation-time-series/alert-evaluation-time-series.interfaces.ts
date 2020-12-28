import { AlertEvaluation } from "../../rest/dto/alert.interfaces";

export interface AlertEvaluationTimeSeriesProps {
    alertEvaluation: AlertEvaluation | null;
}

export interface AlertEvaluationTimeSeriesInternalProps {
    width: number;
    height: number;
    alertEvaluation: AlertEvaluation | null;
}

export enum AlertEvaluationTimeSeriesPlot {
    UPPER_AND_LOWER_BOUND = "UPPER_AND_LOWER_BOUND",
    CURRENT = "CURRENT",
    BASELINE = "BASELINE",
    ANOMALIES = "ANOMALIES",
}

export interface AlertEvaluationTimeSeriesPoint {
    timestamp: number;
    upperBound: number;
    lowerBound: number;
    current: number;
    expected: number;
}
