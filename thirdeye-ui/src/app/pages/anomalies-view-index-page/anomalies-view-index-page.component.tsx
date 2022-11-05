import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { PageHeader } from "../../components/page-header/page-header.component";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    PageContentsGridV1,
    PageV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesAnomalyViewPath } from "../../utils/routes/routes.util";
import { WEEK_IN_MILLISECONDS } from "../../utils/time/time.util";
import { AnomaliesViewPageParams } from "../anomalies-view-page/anomalies-view-page.interfaces";

/**
 * The sole purpose of this page is to figure out the default dates
 * to redirect to the anomalies view page to
 */
export const AnomaliesViewIndexPage: FunctionComponent = () => {
    const {
        anomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages,
    } = useGetAnomaly();
    const params = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    useEffect(() => {
        params.id &&
            isValidNumberId(params.id) &&
            getAnomaly(toNumber(params.id));
    }, []);

    useEffect(() => {
        if (anomaly) {
            const timeRangeQuery = new URLSearchParams([
                [TimeRangeQueryStringKey.TIME_RANGE, TimeRange.CUSTOM],
                [
                    TimeRangeQueryStringKey.START_TIME,
                    (anomaly.startTime - WEEK_IN_MILLISECONDS * 2).toString(),
                ],
                [
                    TimeRangeQueryStringKey.END_TIME,
                    (anomaly.endTime + WEEK_IN_MILLISECONDS * 2).toString(),
                ],
            ]);
            navigate(
                `${getAnomaliesAnomalyViewPath(
                    toNumber(params.id)
                )}?${timeRangeQuery.toString()}`,
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

    return (
        <PageV1>
            <PageHeader>
                <SkeletonV1 />
            </PageHeader>

            <PageContentsGridV1>
                {/* Anomaly */}
                <Grid item xs={12}>
                    <SkeletonV1 />
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    <SkeletonV1 />
                </Grid>

                {/* Existing investigations */}
                <Grid item xs={12}>
                    <SkeletonV1 />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
