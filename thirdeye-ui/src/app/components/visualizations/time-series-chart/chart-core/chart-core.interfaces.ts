import { AxisScale } from "@visx/axis";
import { UseTooltipParams } from "@visx/tooltip/lib/hooks/useTooltip";
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
    tooltipUtils?: UseTooltipParams<{ xValue: number }>;
    top?: number;
    left?: number;
    children?: (
        xScale: AxisScale<number>,
        yScale: AxisScale<number>
    ) => React.ReactElement | undefined;
    yAccessor?: (d: DataPoint) => number;
    xAccessor?: (d: DataPoint) => Date;
    colorScale: (name: string) => string;
}
