/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AxisScale } from "@visx/axis";
import { UseTooltipParams } from "@visx/tooltip/lib/hooks/useTooltip";
import {
    EventWithChartState,
    NormalizedSeries,
} from "../time-series-chart.interfaces";

export interface EventsChartProps {
    series: NormalizedSeries[];
    events: EventWithChartState[];
    xScale?: AxisScale<number>;
    width: number;
    xMax: number;
    margin: { top: number; right: number; bottom: number; left: number };
    tooltipUtils: UseTooltipParams<{ xValue: number }>;
    left?: number;
    isTooltipEnabled: boolean;
}

export interface EventsTooltipPopoverProps {
    events: EventWithChartState[];
    colorScale: (id: number) => string;
}
