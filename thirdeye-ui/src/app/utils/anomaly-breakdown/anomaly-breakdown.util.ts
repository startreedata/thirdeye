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
import { OFFSET_TO_MILLISECONDS } from "../../components/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.utils";
import {
    BaselineOffsetUnitsKey,
    OFFSET_TO_HUMAN_READABLE,
} from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";

export const comparisonOffsetReadableValue = (
    offsetTime: number,
    baselineOption: BaselineOffsetUnitsKey
): string => {
    const baseline = OFFSET_TO_HUMAN_READABLE[baselineOption];

    if (offsetTime === 1) {
        return `${offsetTime} ${baseline.toLowerCase()} ago`;
    }

    return `${offsetTime} ${baseline.toLowerCase()}s ago`;
};

export const parseBaselineComparisonOffset = (
    offset: string
): { baselineOffsetValue: number; unit: BaselineOffsetUnitsKey } => {
    const value = Number(offset.slice(1, 2));
    const unit = offset.slice(2) as BaselineOffsetUnitsKey;

    const validBaselineUnit = checkIfAnomalyBreakdownAPIOffsetUnitsKey(unit)
        ? unit
        : BaselineOffsetUnitsKey.WEEK;
    const validOffsetValue = isNaN(value) ? 1 : value;

    return { baselineOffsetValue: validOffsetValue, unit: validBaselineUnit };
};

export const baselineComparisonOffsetToHumanReadable = (
    value: number,
    unit: BaselineOffsetUnitsKey
): string => {
    return `${value} ${
        OFFSET_TO_HUMAN_READABLE[unit as BaselineOffsetUnitsKey]
    }${value !== 1 ? "s" : ""} ago`;
};

export const baselineOffsetToMilliseconds = (
    value: number,
    unit: BaselineOffsetUnitsKey
): number => {
    return value * OFFSET_TO_MILLISECONDS[unit];
};

// Type guard for baselineOffsetUnit
const checkIfAnomalyBreakdownAPIOffsetUnitsKey = (
    value: string
): value is BaselineOffsetUnitsKey => {
    return (
        OFFSET_TO_HUMAN_READABLE[value as BaselineOffsetUnitsKey] !== undefined
    );
};
