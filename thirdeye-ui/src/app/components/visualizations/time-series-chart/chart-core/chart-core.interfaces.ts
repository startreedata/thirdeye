/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
