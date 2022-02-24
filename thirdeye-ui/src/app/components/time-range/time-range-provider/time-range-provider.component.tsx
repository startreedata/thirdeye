import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { useTimeRangeStore } from "../../../stores/time-range/time-range.store";
import {
    getTimeRangeDurationFromQueryString,
    useSetQueryParamsUtil,
} from "../../../utils/params/params.util";
import {
    TimeRange,
    TimeRangeProviderProps,
    UseTimeRangeProps,
} from "./time-range-provider.interfaces";

export const TimeRangeProvider: FunctionComponent<TimeRangeProviderProps> = (
    props: TimeRangeProviderProps
) => {
    const setUrlQueryParamsUtils = useSetQueryParamsUtil();
    const [loading, setLoading] = useState(true);
    const [timeRangeDuration, setTimeRangeDuration, refreshTimeRange] =
        useTimeRangeStore((state) => [
            state.timeRangeDuration,
            state.setTimeRangeDuration,
            state.refreshTimeRange,
        ]);
    const location = useLocation();

    useEffect(() => {
        // When loading for the first time, initialize time range
        initTimeRange();
        setLoading(false);
    }, []);

    useEffect(() => {
        if (loading) {
            return;
        }

        // Time range changed, add time range duration to query string
        setUrlQueryParamsUtils.setTimeRangeDurationInQueryString(
            timeRangeDuration
        );
    }, [timeRangeDuration]);

    useEffect(() => {
        if (loading) {
            return;
        }

        // Use any navigation opportunity to refresh time range
        refreshTimeRange();
    }, [location.pathname]);

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
