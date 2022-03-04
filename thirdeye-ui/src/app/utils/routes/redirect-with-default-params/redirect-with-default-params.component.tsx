import React, { FunctionComponent, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useTimeRange } from "../../../components/time-range/time-range-provider/time-range-provider.component";
import { RedirectWithDefaultParamsProps } from "./redirect-with-default-params.interfaces";

/**
 * Redirects to the given path with the time range parameters in the search params
 *
 * @param {string} to - Path to redirect to
 * @param {boolean} replace - Indicates to replace the history entry with the new path
 */
export const RedirectWithDefaultParams: FunctionComponent<
    RedirectWithDefaultParamsProps
> = ({ to, replace = true, children }) => {
    const navigate = useNavigate();
    const { timeRangeDuration } = useTimeRange();
    const timeRangeQuery = new URLSearchParams([
        ["timeRange", timeRangeDuration.timeRange],
        ["startTime", timeRangeDuration.startTime.toString()],
        ["endTime", timeRangeDuration.endTime.toString()],
    ]);

    useEffect(() => {
        navigate(`${to}?${timeRangeQuery.toString()}`, { replace });
    });

    return <>{children}</>;
};
