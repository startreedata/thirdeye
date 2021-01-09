import { isEqual } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import {
    getTimeRangeDurationFromQueryString,
    setTimeRangeDurationInQueryString,
} from "../../utils/params-util/params-util";
import { TimeRange } from "../time-range-selector/time-range-selector.interfaces";
import { AppTimeRangeHousekeeperProps } from "./app-time-range-housekeeper.interfaces";

export const AppTimeRangeHousekeeper: FunctionComponent<AppTimeRangeHousekeeperProps> = (
    props: AppTimeRangeHousekeeperProps
) => {
    const [loading, setLoading] = useState(true);
    const [
        appTimeRangeDuration,
        setAppTimeRangeDuration,
        refreshAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.setAppTimeRangeDuration,
        state.refreshAppTimeRangeDuration,
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
        setTimeRangeDurationInQueryString(appTimeRangeDuration);
    }, [appTimeRangeDuration]);

    useEffect(() => {
        if (loading) {
            return;
        }

        // Use any navigation opportunity to refresh app time range
        refreshAppTimeRangeDuration();
    }, [location.pathname]);

    const initAppTimeRange = (): void => {
        // Pick up time range duration from query string
        const timeRageDuration = getTimeRangeDurationFromQueryString();

        // If time range duration from query string is available and different from current app
        // time range duration, update it to app time range store as a custom time range duration
        if (
            timeRageDuration &&
            !isEqual(timeRageDuration, appTimeRangeDuration)
        ) {
            timeRageDuration.timeRange = TimeRange.CUSTOM;
            setAppTimeRangeDuration(timeRageDuration);

            return;
        }

        // If time range duration from query string is not available, refresh existing app time
        // range duration
        refreshAppTimeRangeDuration();
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
