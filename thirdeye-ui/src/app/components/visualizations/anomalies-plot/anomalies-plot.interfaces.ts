import { ScaleLinear, ScaleTime } from "d3-scale";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

export interface AnomaliesPlotProps {
    anomalies: Anomaly[];
    xScale: ScaleTime<number, number>;
    yScale: ScaleLinear<number, number>;
}
