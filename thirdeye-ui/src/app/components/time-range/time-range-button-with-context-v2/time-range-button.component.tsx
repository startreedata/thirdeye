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
import React, { FunctionComponent, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { TimeRangeButton } from "../time-range-button-v2/time-range-button.component";
import { useTimeRange } from "../time-range-provider/time-range-provider.component";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeQueryStringKey,
} from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeButtonWithContextProps } from "./time-range-button.interfaces";

export const TimeRangeButtonWithContext: FunctionComponent<TimeRangeButtonWithContextProps> =
    ({
        onTimeRangeChange,
        maxDate,
        minDate,
        btnGroupColor = "secondary",
        hideQuickExtend,
        timezone,
        defaultTimeRange,
    }) => {
        const { recentCustomTimeRangeDurations, setTimeRangeDuration } =
            useTimeRange();
        const [searchParams, setSearchParams] = useSearchParams();
        const timeRangeDuration = useMemo(() => {
            if (!defaultTimeRange) {
                return {
                    [TimeRangeQueryStringKey.TIME_RANGE]: TimeRange.CUSTOM,
                    [TimeRangeQueryStringKey.START_TIME]: Number(
                        searchParams.get(TimeRangeQueryStringKey.START_TIME)
                    ),
                    [TimeRangeQueryStringKey.END_TIME]: Number(
                        searchParams.get(TimeRangeQueryStringKey.END_TIME)
                    ),
                };
            }

            return defaultTimeRange;
        }, [searchParams, defaultTimeRange]);

        const onHandleTimeRangeChange = (
            timeRangeDuration: TimeRangeDuration
        ): void => {
            setTimeRangeDuration(timeRangeDuration);
            searchParams.set(
                TimeRangeQueryStringKey.TIME_RANGE,
                timeRangeDuration.timeRange
            );
            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                timeRangeDuration.startTime.toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                timeRangeDuration.endTime.toString()
            );
            setSearchParams(searchParams);

            if (onTimeRangeChange) {
                onTimeRangeChange(
                    timeRangeDuration.startTime,
                    timeRangeDuration.endTime
                );
            }
        };

        return (
            <TimeRangeButton
                btnGroupColor={btnGroupColor}
                hideQuickExtend={hideQuickExtend}
                maxDate={maxDate}
                minDate={minDate}
                recentCustomTimeRangeDurations={recentCustomTimeRangeDurations}
                timeRangeDuration={timeRangeDuration}
                timezone={timezone}
                onChange={onHandleTimeRangeChange}
            />
        );
    };
