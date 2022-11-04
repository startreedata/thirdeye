// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useLocation, useSearchParams } from "react-router-dom";
import { useTimeRangeStore } from "../../../stores/time-range/time-range.store";
import { getTimeRangeDurationFromQueryString } from "../../../utils/params/params.util";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeProviderProps,
    TimeRangeQueryStringKey,
    UseTimeRangeProps,
} from "./time-range-provider.interfaces";

export const TimeRangeProvider: FunctionComponent<TimeRangeProviderProps> = (
    props: TimeRangeProviderProps
) => {
    const [loading, setLoading] = useState(true);
    const [timeRangeDuration, setTimeRangeDuration, refreshTimeRange] =
        useTimeRangeStore((state) => [
            state.timeRangeDuration,
            state.setTimeRangeDuration,
            state.refreshTimeRange,
        ]);
    const location = useLocation();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        // When loading for the first time, initialize time range
        initTimeRange();
        setLoading(false);
    }, []);

    useEffect(() => {
        if (loading) {
            return;
        }

        // Use any navigation opportunity to refresh time range
        refreshTimeRange();
    }, [location.pathname]);

    // Sync search params if they changed for the time range duration
    useEffect(() => {
        if (loading) {
            return;
        }

        const queryStringTimeRageDuration = {
            [TimeRangeQueryStringKey.TIME_RANGE]: searchParams.get(
                TimeRangeQueryStringKey.TIME_RANGE
            ),
            [TimeRangeQueryStringKey.START_TIME]: Number(
                searchParams.get(TimeRangeQueryStringKey.START_TIME)
            ),
            [TimeRangeQueryStringKey.END_TIME]: Number(
                searchParams.get(TimeRangeQueryStringKey.END_TIME)
            ),
        };

        if (
            !queryStringTimeRageDuration[TimeRangeQueryStringKey.TIME_RANGE] ||
            !queryStringTimeRageDuration[TimeRangeQueryStringKey.START_TIME] ||
            !queryStringTimeRageDuration[TimeRangeQueryStringKey.END_TIME]
        ) {
            return;
        }

        if (
            queryStringTimeRageDuration &&
            !isEqual(queryStringTimeRageDuration, timeRangeDuration)
        ) {
            queryStringTimeRageDuration.timeRange = TimeRange.CUSTOM;
            setTimeRangeDuration(
                queryStringTimeRageDuration as TimeRangeDuration
            );
        }
    }, [searchParams]);

    const initTimeRange = (): void => {
        // Pick up time range duration from query string
        const queryStringTimeRageDuration =
            getTimeRangeDurationFromQueryString();
        // If time range duration from query string is available and different from current time
        // range duration, update it to time range store as a custom time range duration
        if (
            queryStringTimeRageDuration &&
            !isEqual(queryStringTimeRageDuration, timeRangeDuration)
        ) {
            queryStringTimeRageDuration.timeRange = TimeRange.CUSTOM;
            setTimeRangeDuration(queryStringTimeRageDuration);

            return;
        }

        // If time range duration from query string is not available, refresh existing time range
        refreshTimeRange();
    };

    if (loading) {
        return <></>;
    }

    return <>{props.children}</>;
};

export const useTimeRange = (): UseTimeRangeProps => {
    return useTimeRangeStore((state) => ({
        timeRangeDuration: state.timeRangeDuration,
        recentCustomTimeRangeDurations: state.recentCustomTimeRangeDurations,
        setTimeRangeDuration: state.setTimeRangeDuration,
        refreshTimeRange: state.refreshTimeRange,
    }));
};
