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
import { Bounds } from "@visx/brush/lib/types";
import {
    NormalizedSeries,
    XAxisOptions,
    ZoomDomain,
} from "../time-series-chart.interfaces";

export interface ChartBrushProps {
    series: NormalizedSeries[];
    height: number;
    width: number;
    top: number;
    colorScale: (name: string) => string;
    onBrushChange: (domain: Bounds | null) => void;
    onBrushClick: () => void;
    xAxisOptions?: XAxisOptions;
    initialZoom?: ZoomDomain;
    margins: {
        left: number;
        right: number;
        bottom: number;
        top: number;
    };
    onMouseEnter: () => void;
}
