import { Anomaly } from "../../rest/dto/anomaly.interfaces";

export interface TimeSeriesChartProps {
    height: number;
    width: number;
    margin: Margin;
    data: TimeSeriesProps[];
    anomalies: Anomaly[];
    compact?: boolean;
    showLegend?: boolean;
}

export interface TimeSeriesProps {
    timestamp: Date;
    current: number;
    expacted: number;
    upperBound: number;
    lowerBound: number;
}

export interface Margin {
    left: number;
    right: number;
    top: number;
    bottom: number;
}
