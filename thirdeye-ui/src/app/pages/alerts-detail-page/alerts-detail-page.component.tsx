import { Grid } from "@material-ui/core";
import { cloneDeep, isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { AlertCardData } from "../../components/alert-card/alert-card.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getAlert,
    getAlertEvaluation,
    updateAlert,
} from "../../rest/alert-rest/alert-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import { getAlertCardData } from "../../utils/alert-util/alert-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getAlertsDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import { AlertsDetailPageParams } from "./alerts-detail-page.interfaces";

export const AlertsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState<AlertCardData>({} as AlertCardData);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<AlertsDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    const [
        appTimeRange,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRange,
        state.getAppTimeRangeDuration,
    ]);

    const [chartData, setChartData] = useState<AlertEvaluation | null>(
        {} as AlertEvaluation
    );

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            return;
        }

        let alert = {} as AlertCardData;
        const [
            alertResponse,
            subscriptionGroupsResponse,
        ] = await Promise.allSettled([
            getAlert(toNumber(params.id)),
            getAllSubscriptionGroups(),
        ]);

        if (
            alertResponse.status === "rejected" ||
            subscriptionGroupsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            alert = getAlertCardData(
                alertResponse.value,
                subscriptionGroupsResponse.value
            );

            // Create page breadcrumbs
            setPageBreadcrumbs([
                {
                    text: alert.name,
                    path: getAlertsDetailPath(alert.id),
                },
            ]);
        }

        setAlert(alert);
    };

    // To fetch chartData on dateRange Change
    useEffect(() => {
        const init = async (): Promise<void> => {
            setChartData(null);

            setChartData(await fetchChartData());
        };

        init();
    }, [appTimeRange, params.id]);

    const fetchChartData = async (): Promise<AlertEvaluation | null> => {
        const { startTime, endTime } = getAppTimeRangeDuration();

        if (!isValidNumberId(params.id) || !startTime || !endTime) {
            // To turn off the loader
            return {} as AlertEvaluation;
        }

        let chartData = null;

        try {
            const alertEvalution = {
                alert: ({ id: params.id } as unknown) as Alert,
                start: startTime,
                end: endTime,
            } as AlertEvaluation;

            chartData = await getAlertEvaluation(alertEvalution);
        } catch (e) {
            // Empty block
            chartData = {} as AlertEvaluation;
        }

        return chartData;
    };

    const onAlertStateToggle = async (
        alertCardData: AlertCardData
    ): Promise<void> => {
        if (!alertCardData.alert) {
            return;
        }

        let alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        try {
            alertCopy = await updateAlert(alertCopy);

            fetchData();
        } catch (error) {
            enqueueSnackbar(
                t("message.update-error", { entity: t("label.alert") }),
                SnackbarOption.ERROR
            );
        }
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents centerAlign title={alert.name ? alert.name : ""}>
                {!isEmpty(alert) && (
                    <Grid container>
                        <Grid item md={12}>
                            <AlertCard
                                hideViewDetailsLinks
                                alert={alert}
                                onAlertStateToggle={onAlertStateToggle}
                            />
                        </Grid>

                        <Grid item md={12}>
                            <AlertEvaluationTimeSeriesCard
                                alertEvaluation={chartData}
                            />
                        </Grid>
                    </Grid>
                )}
            </PageContents>
        </PageContainer>
    );
};
