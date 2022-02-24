import { ScaleLinear, ScaleTime } from "d3-scale";
import { Anomaly } from "../../../../rest/dto/anomaly.interfaces";
import { AlertEvaluationTimeSeriesPoint } from "../alert-evaluation-time-series/alert-evaluation-time-series.interfaces";

export interface AlertEvaluationTimeSeriesPlotProps {
    current?: boolean;
    baseline?: boolean;
    upperAndLowerBound?: boolean;
    anomalies?: boolean;
    alertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[];
    alertEvaluationAnomalies?: Anomaly[];
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
    onAnomalyBarClick?: (anomaly: Anomaly) => void;
}
