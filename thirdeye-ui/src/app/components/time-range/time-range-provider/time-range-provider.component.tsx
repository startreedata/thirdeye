import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { useTimeRangeStore } from "../../../store/time-range-store/time-range-store";
import {
    getTimeRangeDurationFromQueryString,
    setTimeRangeDurationInQueryString,
} from "../../../utils/params-util/params-util";
import {
    TimeRange,
    TimeRangeProviderProps,
    UseTimeRangeProps,
} from "./time-range-provider.interfaces";

export const TimeRangeProvider: FunctionComponent<TimeRangeProviderProps> = (
    props: TimeRangeProviderProps
) => {
    const [loading, setLoading] = useState(true);
    const [
        timeRangeDuration,
        setTimeRangeDuration,
        refreshTimeRange,
    ] = useTimeRangeStore((state) => [
        state.timeRangeDuration,
        state.setTimeRangeDuration,
        state.refreshTimeRange,
    ]);
    const location = useLocation();

    useEffect(() => {
        // When loading for the first time, initialize app time range
        initAppTimeRange();

        setLoading(false);
    }, []);

    useEffect(() => {
        if (loading) {
            return;
        }

        // App time range changed, add app time range durtion to query string
        setTimeRangeDurationInQueryString(timeRangeDuration);
    }, [timeRangeDuration]);

    useEffect(() => {
        if (loading) {
            return;
        }

        // Use any navigation opportunity to refresh app time range
        refreshTimeRange();
    }, [location.pathname]);

    const initAppTimeRange = (): void => {
        // Pick up time range duration from query string
        const timeRageDuration = getTimeRangeDurationFromQueryString();

        // If time range duration from query string is available and different from current app
        // time range duration, update it to app time range store as a custom time range duration
        if (timeRageDuration && !isEqual(timeRageDuration, timeRangeDuration)) {
            timeRageDuration.timeRange = TimeRange.CUSTOM;
            setTimeRangeDuration(timeRageDuration);

            return;
        }

        // If time range duration from query string is not available, refresh existing app time
        // range
        refreshTimeRange();
    };

    if (loading) {
        return <></>;
    }

    return (
        <>
            {/* Contents */}
            {props.children}
        </>
    );
};

export const useTimeRange = (): UseTimeRangeProps => {
    return useTimeRangeStore((store) => ({
        timeRangeDuration: store.timeRangeDuration,
        recentCustomTimeRangeDurations: store.recentCustomTimeRangeDurations,
        setTimeRangeDuration: store.setTimeRangeDuration,
        refreshTimeRange: store.refreshTimeRange,
    }));
};
