import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useHistory, useParams } from "react-router-dom";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { TimeRange } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { WEEK_IN_MILLISECONDS } from "../../utils/date-time/date-time.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getRootCauseAnalysisForAnomalyPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

export const RootCauseAnalysisForAnomalyIndexPage: FunctionComponent = () => {
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
                startTime: anomaly.startTime - WEEK_IN_MILLISECONDS * 2,
                endTime: anomaly.endTime + WEEK_IN_MILLISECONDS * 2,
            });
            history.replace(
                getRootCauseAnalysisForAnomalyPath(toNumber(params.id))
            );
        }
    }, [anomaly]);

    return <AppLoadingIndicatorV1 />;
};
