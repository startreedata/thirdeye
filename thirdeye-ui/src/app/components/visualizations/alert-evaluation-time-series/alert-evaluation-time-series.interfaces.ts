import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";

export interface AlertEvaluationTimeSeriesProps {
    alertEvaluation: AlertEvaluation | null;
}

export interface AlertEvaluationTimeSeriesInternalProps {
    height: number;
    width: number;
    alertEvaluation: AlertEvaluation | null;
}

export interface AlertEvaluationTimeSeriesInternalState {
    loading: boolean;
    noData: boolean;
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    filteredAlertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    alertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[];
    filteredAlertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[];
    currentPlotVisible: boolean;
    baselinePlotVisible: boolean;
    upperAndLowerBoundPlotVisible: boolean;
    anomaliesPlotVisible: boolean;
}

export enum AlertEvaluationTimeSeriesInternalStateAction {
    UPDATE,
    TOGGLE_CURRENT_PLOT_VISIBLE,
    TOGGLE_BASELINE_PLOT_VISIBLE,
    TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE,
    TOGGLE_ANOMALIES_PLOT_VISIBLE,
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

export interface AlertEvaluationTimeSeriesTooltipPoint {
    timestamp: number;
    current: number;
    expected: number;
    upperBound: number;
    lowerBound: number;
    anomalies: AlertEvaluationAnomalyPoint[];
}
