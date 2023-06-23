/*
 * Copyright 2023 StarTree Inc
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

import { serializeKeyValuePair } from "../../../utils/params/params.util";
import { COLOR_PALETTE } from "../../visualizations/time-series-chart/time-series-chart.utils";
import { AnomalyFilterOption } from "../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";

/**
 * Simple method for generating a number for a string
 * @param str
 */
export const numberForStr = (str: string): number => {
    return (
        str
            .split("")
            .map((i) => i.charCodeAt(0))
            .reduce((a, b) => a + b, 0) % 67
    );
};

export const getColorForStr = (str: string, color: string[]): string => {
    return color[numberForStr(str) % color.length];
};

export const getColorForDimensionCombo = (
    dimensionCombination: AnomalyFilterOption[]
): string => {
    const serializedStr = serializeKeyValuePair(dimensionCombination);

    return getColorForStr(serializedStr, COLOR_PALETTE);
};
