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

import { ReactNode } from "react";

export interface TimeRangeProviderProps {
    children: ReactNode;
}

export interface UseTimeRangeProps {
    timeRangeDuration: TimeRangeDuration; // Current time range duration
    recentCustomTimeRangeDurations: TimeRangeDuration[]; // An ordered list of last few used custom time range durations
    setTimeRangeDuration: (timeRangeDuration: TimeRangeDuration) => void; // Sets time range duration
    refreshTimeRange: () => void; // Refreshes time range so that listeners pick up the latest time range duration
}

export interface TimeRangeDuration {
    timeRange: TimeRange;
    startTime: number;
    endTime: number;
}

export enum TimeRange {
    CUSTOM = "CUSTOM",
    LAST_15_MINUTES = "LAST_15_MINUTES",
    LAST_1_HOUR = "LAST_1_HOUR",
    LAST_12_HOURS = "LAST_12_HOURS",
    LAST_24_HOURS = "LAST_24_HOURS",
    LAST_7_DAYS = "LAST_7_DAYS",
    LAST_30_DAYS = "LAST_30_DAYS",
    TODAY = "TODAY",
    YESTERDAY = "YESTERDAY",
    THIS_WEEK = "THIS_WEEK",
    LAST_WEEK = "LAST_WEEK",
    THIS_MONTH = "THIS_MONTH",
    LAST_MONTH = "LAST_MONTH",
    THIS_YEAR = "THIS_YEAR",
    LAST_YEAR = "LAST_YEAR",
}

export enum TimeRangeQueryStringKey {
    TIME_RANGE = "timeRange",
    START_TIME = "startTime",
    END_TIME = "endTime",
}
