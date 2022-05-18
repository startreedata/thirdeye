import {
    DataPoint,
    ThresholdDataPoint,
} from "../time-series-chart/time-series-chart.interfaces";

export interface StackedLineProps {
    points: DataPoint[];
    paddingTop?: number;
    strokeWidth: number;
    stroke?: string;
    gapBetweenLines?: number;
    x: (d: DataPoint) => number;
    x1: (d: ThresholdDataPoint) => number;
    y: (d: DataPoint) => number;
    y1: (d: ThresholdDataPoint) => number;
}
