import { Grid } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
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
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import {
    createAlertEvaluation,
    createEmptyAlertCardData,
    getAlertCardData,
} from "../../utils/alert-util/alert-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getAlertsDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import { AlertsDetailPageParams } from "./alerts-detail-page.interfaces";

export const AlertsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alertCardData, setAlertCardData] = useState<AlertCardData>(
        createEmptyAlertCardData()
    );
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [
        appTimeRange,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRange,
        state.getAppTimeRangeDuration,
    ]);
    const params = useParams<AlertsDetailPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: alertCardData
                    ? alertCardData.name
                    : t("label.no-data-available-marker"),
                path: alertCardData
                    ? getAlertsDetailPath(alertCardData.id)
                    : "",
            },
        ]);
    }, [alertCardData]);

    useEffect(() => {
        // Fetch data
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, [params.id]);

    useEffect(() => {
        // Fetch visualization data
        const init = async (): Promise<void> => {
            setAlertEvaluation(null);

            await fetchVisualizationData();
        };

        init();
    }, [alertCardData, appTimeRange]);

    const fetchData = async (): Promise<void> => {
        let fetchedAlertCardData = createEmptyAlertCardData();
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            setAlertCardData(fetchedAlertCardData);

            return;
        }

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
            fetchedAlertCardData = getAlertCardData(
                alertResponse.value,
                subscriptionGroupsResponse.value
            );
            fetchedSubscriptionGroups = subscriptionGroupsResponse.value;
        }

        setAlertCardData(fetchedAlertCardData);
        setSubscriptionGroups(fetchedSubscriptionGroups);
    };

    const fetchVisualizationData = async (): Promise<void> => {
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!alertCardData || !alertCardData.alert) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        const timeRangeDuration = getAppTimeRangeDuration();
        try {
            fetchedAlertEvaluation = await getAlertEvaluation(
                createAlertEvaluation(
                    alertCardData.alert,
                    timeRangeDuration.startTime,
                    timeRangeDuration.endTime
                )
            );
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        }

        setAlertEvaluation(fetchedAlertEvaluation);
    };

    const onAlertStateToggle = async (
        alertCardData: AlertCardData
    ): Promise<void> => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        let alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        try {
            alertCopy = await updateAlert(alertCopy);

            // Replace updated alert as fetched alert
            setAlertCardData(getAlertCardData(alertCopy, subscriptionGroups));
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
            <PageContents
                centerAlign
                title={
                    alertCardData
                        ? alertCardData.name
                        : t("label.no-data-available-marker")
                }
            >
                <Grid container>
                    {/* Alert */}
                    <Grid item md={12}>
                        <AlertCard
                            hideViewDetailsLinks
                            alert={alertCardData}
                            onAlertStateToggle={onAlertStateToggle}
                        />
                    </Grid>

                    {/* Alert evaluation time series */}
                    <Grid item md={12}>
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                        />
                    </Grid>
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
