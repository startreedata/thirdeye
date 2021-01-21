import { AlertEvaluationTimeSeriesPlot } from "../alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesLegendProps {
    upperAndLowerBoundVisible?: boolean;
    currentVisible?: boolean;
    baselineVisible?: boolean;
    anomaliesVisible?: boolean;
    onChange: (
        alertEvaluationTimeSeriesPlot: AlertEvaluationTimeSeriesPlot
    ) => void;
}
