import { AxisScale } from "d3";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface AnomalyChartProps {
    anomalies: Anomaly[];
    xScale: AxisScale<Date>;
    yScale: AxisScale<number>;
    dotRadius?: number;
    stroke?: string;
    strokeWidth?: number;
}

export interface TimeSeriesAnomaly {
    timestamp: Date;
    current: number;
}
