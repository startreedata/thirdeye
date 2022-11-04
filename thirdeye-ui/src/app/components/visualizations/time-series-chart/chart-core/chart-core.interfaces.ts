import { AxisScale } from "@visx/axis";
import React, { MouseEvent } from "react";
import {
    NormalizedSeries,
    XAxisOptions,
    YAxisOptions,
} from "../time-series-chart.interfaces";

export interface ChartCoreProps {
    series: NormalizedSeries[];
    xScale?: AxisScale<number>;
    yScale?: AxisScale<number>;
    width: number;
    height: number;
    yMax: number;
    xMax: number;
    margin: { top: number; right: number; bottom: number; left: number };
    showXAxis?: boolean;
    xAxisOptions?: XAxisOptions;
    yAxisOptions?: YAxisOptions;
    showYAxis?: boolean;
    top?: number;
    left?: number;
    children?: (
        xScale: AxisScale<number>,
        yScale: AxisScale<number>
    ) => React.ReactElement | undefined;
    colorScale: (name: string) => string;
    onMouseLeave?: () => void;
    onMouseMove?: (event: MouseEvent<SVGSVGElement>) => void;
    onMouseEnter?: (event: MouseEvent<SVGSVGElement>) => void;
}
