import { AxisScale } from "@visx/axis";
import { Series } from "../time-series-chart.interfaces";

export interface TooltipMarkersProps {
    chartHeight: number;
    xScale: AxisScale<number>;
    yScale: AxisScale<number>;
    series: Series[];
    xValue: number;
    colorScale: (name: string) => string;
}
export interface TooltipPopoverProps {
    xValue: number;
    series: Series[];
    colorScale: (name: string) => string;
}
