import React, { FunctionComponent, useEffect } from "react";
import { resolvePath, useLocation, useNavigate } from "react-router-dom";
import { useTimeRange } from "../../../components/time-range/time-range-provider/time-range-provider.component";
import { TimeRangeQueryStringKey } from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { useLastUsedSearchParams } from "../../../stores/last-used-params/last-used-search-params.store";
import { RedirectWithDefaultParamsProps } from "./redirect-with-default-params.interfaces";

/**
 * Redirects to the given path with the time range parameters in the search params.
 * If useStoredLastUsedParamsPathKey is passed, try to use the last stored search
 * params for the path if it exists
 *
 * @param {string} to - Path to redirect to
 * @param {boolean} replace - Indicates to replace the history entry with the new path
 * @param {string} useStoredLastUsedParamsPathKey - [Optional] If passed, use the stored last used
 * @param {string} pathKeyOverride - [Optional] use this as the key to find in the store
 * search string if it exists
 */
export const RedirectWithDefaultParams: FunctionComponent<
    RedirectWithDefaultParamsProps
> = ({
    to,
    replace = true,
    children,
    useStoredLastUsedParamsPathKey,
    pathKeyOverride,
}) => {
    const location = useLocation();
    const navigate = useNavigate();
    const { getLastUsedForPath } = useLastUsedSearchParams();
    const { timeRangeDuration } = useTimeRange();
    let searchString: string | undefined;

    if (useStoredLastUsedParamsPathKey) {
        const pathKey =
            pathKeyOverride || resolvePath(to, location.pathname).pathname;
        searchString = getLastUsedForPath(pathKey);
    }

    if (!searchString) {
        const timeRangeQuery = new URLSearchParams([
            [TimeRangeQueryStringKey.TIME_RANGE, timeRangeDuration.timeRange],
            [
                TimeRangeQueryStringKey.START_TIME,
                timeRangeDuration.startTime.toString(),
            ],
            [
                TimeRangeQueryStringKey.END_TIME,
                timeRangeDuration.endTime.toString(),
            ],
        ]);
        searchString = timeRangeQuery.toString();
    }

    useEffect(() => {
        navigate(`${to}?${searchString}`, { replace });
    });

    return <>{children}</>;
};
