import { AlertEvaluationTimeSeriesPlotLine } from "../alert-evaluation-time-series/alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesLegendProps {
    current: boolean;
    baseline: boolean;
    upperAndLowerBound: boolean;
    anomalies: boolean;
    parentWidth?: number;
    onChange: (
        alertEvaluationTimeSeriesPlotLine: AlertEvaluationTimeSeriesPlotLine
    ) => void;
}
