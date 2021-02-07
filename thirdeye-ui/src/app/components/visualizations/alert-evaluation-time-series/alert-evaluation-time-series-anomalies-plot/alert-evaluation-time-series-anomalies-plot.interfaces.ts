import { ScaleLinear, ScaleTime } from "d3-scale";
import { AlertEvaluationAnomalyPoint } from "../alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesAnomaliesPlotProps {
    alertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[];
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
}
