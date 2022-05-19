import { AxisScale } from "@visx/axis";
import { NormalizedSeries } from "../time-series-chart/time-series-chart.interfaces";

export interface StackedLineProps {
    paddingTop?: number;
    strokeWidth: number;
    stroke?: string;
    gapBetweenLines?: number;
    series: NormalizedSeries;
    xScale: AxisScale<number>;
    yScale: AxisScale<number>;
}
