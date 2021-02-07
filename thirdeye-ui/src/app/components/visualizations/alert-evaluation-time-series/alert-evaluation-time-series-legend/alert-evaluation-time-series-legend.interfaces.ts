import { AlertEvaluationTimeSeriesPlot } from "../alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesLegendProps {
    current: boolean;
    baseline: boolean;
    upperAndLowerBound: boolean;
    anomalies: boolean;
    onChange: (
        alertEvaluationTimeSeriesPlot: AlertEvaluationTimeSeriesPlot
    ) => void;
}
