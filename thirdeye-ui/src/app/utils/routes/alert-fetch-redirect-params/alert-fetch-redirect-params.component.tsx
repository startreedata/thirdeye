import React, { FunctionComponent, useEffect, useState } from "react";
import {
    resolvePath,
    useLocation,
    useNavigate,
    useParams,
    useSearchParams,
} from "react-router-dom";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
import { useGetAlertInsight } from "../../../rest/alerts/alerts.actions";
import { useLastUsedSearchParams } from "../../../stores/last-used-params/last-used-search-params.store";
import { AlertFetchRedirectParamsProps } from "./alert-fetch-redirect-params.interfaces";

export const AlertFetchRedirectParams: FunctionComponent<
    AlertFetchRedirectParamsProps
> = ({ to, replace = true, children, fallbackDurationGenerator }) => {
    const [isLoading, setIsLoading] = useState(true);
    const { id: alertId } = useParams();
    const { alertInsight, getAlertInsight } = useGetAlertInsight();
    const location = useLocation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { getLastUsedForPath } = useLastUsedSearchParams();
    let searchString: string | undefined;

    useEffect(() => {
        if (alertId) {
            getAlertInsight({ alertId: Number(alertId) }).finally(() =>
                setIsLoading(false)
            );
        } else {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        if (isLoading) {
            return;
        }

        if (alertInsight) {
            searchParams.set(
                TimeRangeQueryStringKey.TIME_RANGE,
                TimeRange.CUSTOM
            );
            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                alertInsight.defaultStartTime.toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                alertInsight.defaultEndTime.toString()
            );
        } else {
            const pathKey = resolvePath(to, location.pathname).pathname;
            searchString = getLastUsedForPath(pathKey);

            if (!searchString) {
                const [customTimeStart, customTimeEnd] =
                    fallbackDurationGenerator();

                searchParams.set(
                    TimeRangeQueryStringKey.TIME_RANGE,
                    TimeRange.CUSTOM
                );
                searchParams.set(
                    TimeRangeQueryStringKey.START_TIME,
                    customTimeStart.toString()
                );
                searchParams.set(
                    TimeRangeQueryStringKey.END_TIME,
                    customTimeEnd.toString()
                );
            }
        }
        searchString = searchParams.toString();
        navigate(`${to}?${searchString}`, { replace });
    }, [isLoading]);

    if (isLoading) {
        return <AppLoadingIndicatorV1 />;
    }

    return <>{children}</>;
};
