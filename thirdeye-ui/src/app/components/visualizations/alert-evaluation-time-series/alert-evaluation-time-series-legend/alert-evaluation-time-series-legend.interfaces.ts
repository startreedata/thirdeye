import { AlertEvaluationTimeSeriesPlotLine } from "../alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesLegendProps {
    current: boolean;
    baseline: boolean;
    upperAndLowerBound: boolean;
    anomalies: boolean;
    onChange: (
        alertEvaluationTimeSeriesPlotLine: AlertEvaluationTimeSeriesPlotLine
    ) => void;
}
