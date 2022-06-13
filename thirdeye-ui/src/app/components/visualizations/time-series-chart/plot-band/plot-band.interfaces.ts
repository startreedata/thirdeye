import { AxisScale } from "@visx/axis";
import { PlotBand } from "../time-series-chart.interfaces";

export interface PlotBandProps {
    plotBand: PlotBand;
    xScale: AxisScale<number>;
    yScale: AxisScale<number>;
}
