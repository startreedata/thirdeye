import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { TimeRange } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { WEEK_IN_MILLISECONDS } from "../../utils/date-time/date-time.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesViewPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

/**
 * The sole purpose of this page is to figure out the default dates
 * to redirect to the anomalies view page to
 */
export const AnomaliesViewIndexPage: FunctionComponent = () => {
    const { anomaly, getAnomaly } = useGetAnomaly();
    const { setTimeRangeDuration } = useTimeRange();
    const params = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();

    useEffect(() => {
        params.id &&
            isValidNumberId(params.id) &&
            getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            setTimeRangeDuration({
                timeRange: TimeRange.CUSTOM,
                startTime: anomaly.startTime - WEEK_IN_MILLISECONDS * 2,
                endTime: anomaly.endTime + WEEK_IN_MILLISECONDS * 2,
            });
            navigate(getAnomaliesViewPath(toNumber(params.id)), {
                replace: true,
            });
        }
    }, [anomaly]);

    return <AppLoadingIndicatorV1 />;
};
