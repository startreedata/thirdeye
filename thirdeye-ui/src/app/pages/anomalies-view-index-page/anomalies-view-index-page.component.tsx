import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useHistory, useParams } from "react-router-dom";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { TimeRange } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesViewPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

const TWO_WEEKS_MS = 2 * 604800000;

/**
 * The sole purpose of this page is to figure out the default dates
 * to redirect to the anomalies view page to
 */
export const AnomaliesViewIndexPage: FunctionComponent = () => {
    const { anomaly, getAnomaly } = useGetAnomaly();
    const { setTimeRangeDuration } = useTimeRange();
    const params = useParams<AnomaliesViewPageParams>();
    const history = useHistory();

    useEffect(() => {
        isValidNumberId(params.id) && getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            setTimeRangeDuration({
                timeRange: TimeRange.CUSTOM,
                startTime: anomaly.startTime - TWO_WEEKS_MS,
                endTime: anomaly.endTime + TWO_WEEKS_MS,
            });
            history.replace(getAnomaliesViewPath(toNumber(params.id)));
        }
    }, [anomaly]);

    return <AppLoadingIndicatorV1 />;
};
