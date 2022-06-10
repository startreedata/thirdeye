// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

export type AnomaliesViewPageParams = {
    id: string;
};

export enum AnomalyBreakdownAPIOffsetValues {
    CURRENT = "current",
    ONE_WEEK_AGO = "P1W",
    TWO_WEEKS_AGO = "P2W",
    THREE_WEEKS_AGO = "P3W",
    FOUR_WEEKS_AGO = "P4W",
}

export const AnomalyBreakdownAPIOffsetsToWeeks = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: 0,
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: 1,
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: 2,
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: 3,
    [AnomalyBreakdownAPIOffsetValues.FOUR_WEEKS_AGO]: 4,
};

export const OFFSET_TO_HUMAN_READABLE = {
    [AnomalyBreakdownAPIOffsetValues.CURRENT]: "",
    [AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO]: "One Week Ago",
    [AnomalyBreakdownAPIOffsetValues.TWO_WEEKS_AGO]: "Two Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.THREE_WEEKS_AGO]: "Three Weeks Ago",
    [AnomalyBreakdownAPIOffsetValues.FOUR_WEEKS_AGO]: "Four Weeks Ago",
};

export const BASELINE_OPTIONS: {
    key: AnomalyBreakdownAPIOffsetValues;
    description: string;
}[] = [];

Object.values(AnomalyBreakdownAPIOffsetValues).forEach(
    (offsetKey: AnomalyBreakdownAPIOffsetValues) => {
        if (offsetKey !== AnomalyBreakdownAPIOffsetValues.CURRENT) {
            BASELINE_OPTIONS.push({
                key: offsetKey,
                description: OFFSET_TO_HUMAN_READABLE[offsetKey],
            });
        }
    }
);
