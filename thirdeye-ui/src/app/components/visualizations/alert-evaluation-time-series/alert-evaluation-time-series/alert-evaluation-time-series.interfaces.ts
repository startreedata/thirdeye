import { AlertEvaluation } from "../../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../../rest/dto/anomaly.interfaces";

export interface AlertEvaluationTimeSeriesProps {
    alertEvaluation: AlertEvaluation | null;
    hideBrush?: boolean;
}

export interface AlertEvaluationTimeSeriesInternalProps {
    parentHeight: number;
    parentWidth: number;
    alertEvaluation: AlertEvaluation | null;
    hideBrush?: boolean;
}

export interface AlertEvaluationTimeSeriesState {
    loading: boolean;
    noData: boolean;
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    filteredAlertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    alertEvaluationAnomalies: Anomaly[];
    filteredAlertEvaluationAnomalies: Anomaly[];
    currentPlotVisible: boolean;
    baselinePlotVisible: boolean;
    upperAndLowerBoundPlotVisible: boolean;
    anomaliesPlotVisible: boolean;
}

export enum AlertEvaluationTimeSeriesStateAction {
    UPDATE,
    TOGGLE_CURRENT_PLOT_VISIBLE,
    TOGGLE_BASELINE_PLOT_VISIBLE,
    TOGGLE_UPPER_AND_LOWER_BOUND_PLOT_VISIBLE,
    TOGGLE_ANOMALIES_PLOT_VISIBLE,
}

export enum AlertEvaluationTimeSeriesPlotLine {
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
