import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    AppLoadingIndicatorV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../utils/routes/routes.util";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

export const RootCauseAnalysisForAnomalyIndexPage: FunctionComponent = () => {
    const [searchParams] = useSearchParams();
    const {
        anomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages,
    } = useGetAnomaly();
    const params = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        !!params.id &&
            isValidNumberId(params.id) &&
            getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            searchParams.set(
                TimeRangeQueryStringKey.TIME_RANGE,
                TimeRange.CUSTOM
            );
            searchParams.set(
                TimeRangeQueryStringKey.START_TIME,
                (anomaly.startTime - WEEK_IN_MILLISECONDS * 2).toString()
            );
            searchParams.set(
                TimeRangeQueryStringKey.END_TIME,
                (anomaly.endTime + WEEK_IN_MILLISECONDS * 2).toString()
            );

            navigate(
                `${getRootCauseAnalysisForAnomalyInvestigatePath(
                    toNumber(params.id)
                )}?${searchParams.toString()}`,
                {
                    replace: true,
                }
            );
        }
    }, [anomaly]);

    useEffect(() => {
        notifyIfErrors(
            anomalyRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomaly"),
            })
        );
    }, [anomalyRequestStatus, errorMessages]);

    return <AppLoadingIndicatorV1 />;
};
