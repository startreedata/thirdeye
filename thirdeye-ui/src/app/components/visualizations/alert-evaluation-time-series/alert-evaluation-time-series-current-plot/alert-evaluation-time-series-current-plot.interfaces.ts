import { ScaleLinear, ScaleTime } from "d3-scale";
import { AlertEvaluationTimeSeriesPoint } from "../alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesCurrentPlotProps {
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
}
