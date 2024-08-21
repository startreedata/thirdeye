/*
 * Copyright 2024 StarTree Inc
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
import { ReactElement } from "react";

export type BarData = { date: string; [key: string]: unknown };
export type GraphLegend = { text: string; value: string };
export interface BarGraphProps {
    width?: number;
    height?: number;
    data: BarData[];
    keysColorMapping: { [key: string]: string };
    tooltipRenderer?: (tooltipData: unknown) => ReactElement;
    margins?: { [key: string]: number };
    graphLegend: GraphLegend[];
}

export interface TooltipProps {
    tooltipOpen?: boolean;
    tooltipLeft?: number;
    tooltipTop?: number;
    tooltipData?: { [key: string]: unknown };
    hideTooltip?: () => void;
    showTooltip?: (tooltipData: { [key: string]: unknown }) => void;
}

export interface LegendProps {
    labels: GraphLegend[];
}
