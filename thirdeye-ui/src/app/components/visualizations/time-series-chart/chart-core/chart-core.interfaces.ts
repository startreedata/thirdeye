import { AxisScale } from "@visx/axis";
import React from "react";
import {
    DataPoint,
    Series,
    XAxisOptions,
} from "../time-series-chart.interfaces";

export interface ChartCoreProps {
    series: Series[];
    xScale?: AxisScale<number>;
    yScale?: AxisScale<number>;
    width: number;
    yMax: number;
    xMax: number;
    margin: { top: number; right: number; bottom: number; left: number };
    showXAxis?: boolean;
    xAxisOptions?: XAxisOptions;
    showYAxis?: boolean;
    top?: number;
    left?: number;
    children?: React.ReactNode;
    yAccessor?: (d: DataPoint) => number;
    xAccessor?: (d: DataPoint) => Date;
    colorScale: (name: string) => string;
}
