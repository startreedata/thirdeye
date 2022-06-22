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
export type AnomaliesViewPageParams = {
    id: string;
};

export enum BaselineOffsetUnitsKey {
    DAY = "D",
    WEEK = "W",
    MONTH = "M",
    YEAR = "Y",
}

export type BaselineOffsetUnitsValues = "Day" | "Week" | "Month" | "Year";

export const OFFSET_TO_HUMAN_READABLE: Record<
    BaselineOffsetUnitsKey,
    BaselineOffsetUnitsValues
> = {
    [BaselineOffsetUnitsKey.DAY]: "Day",
    [BaselineOffsetUnitsKey.WEEK]: "Week",
    [BaselineOffsetUnitsKey.MONTH]: "Month",
    [BaselineOffsetUnitsKey.YEAR]: "Year",
};

export const BASELINE_OPTIONS: {
    key: BaselineOffsetUnitsKey;
    description: BaselineOffsetUnitsValues;
}[] = [];

Object.values(BaselineOffsetUnitsKey).forEach(
    (offsetKey: BaselineOffsetUnitsKey) => {
        BASELINE_OPTIONS.push({
            key: offsetKey,
            description: OFFSET_TO_HUMAN_READABLE[offsetKey],
        });
    }
);
