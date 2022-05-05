import { AxisScale } from "@visx/axis";
import { NormalizedSeries } from "../time-series-chart.interfaces";

export interface TooltipMarkersProps {
    chartHeight: number;
    xScale: AxisScale<number>;
    yScale: AxisScale<number>;
    series: NormalizedSeries[];
    xValue: number;
    colorScale: (name: string) => string;
}
export interface TooltipPopoverProps {
    xValue: number;
    series: NormalizedSeries[];
    colorScale: (name: string) => string;
}
