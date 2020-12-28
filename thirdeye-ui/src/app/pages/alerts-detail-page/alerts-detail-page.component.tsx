import { Grid } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { AlertCard } from "../../components/alert-card/alert-card.component";
import { AlertCardData } from "../../components/alert-card/alert-card.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    updateAlert,
} from "../../rest/alert-rest/alert-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppTimeRangeStore } from "../../store/app-time-range-store/app-time-range-store";
import {
    createAlertEvaluation,
    getAlertCardData,
} from "../../utils/alert-util/alert-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import {
    getAlertsAllPath,
    getAlertsDetailPath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";
import { AlertsDetailPageParams } from "./alerts-detail-page.interfaces";

export const AlertsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alertCardData, setAlertCardData] = useState<AlertCardData>();
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
        appTimeRangeDuration,
        getAppTimeRangeDuration,
    ] = useAppTimeRangeStore((state) => [
        state.appTimeRangeDuration,
        state.getAppTimeRangeDuration,
    ]);
    const params = useParams<AlertsDetailPageParams>();
    const history = useHistory();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: alertCardData ? alertCardData.name : "",
                pathFn: (): string => {
                    return alertCardData
                        ? getAlertsDetailPath(alertCardData.id)
                        : "";
                },
            },
        ]);
    }, [alertCardData]);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, [params.id]);

    useEffect(() => {
        // Fetch visualization data
        fetchVisualizationData();
    }, [alertCardData && alertCardData.id, appTimeRangeDuration]);

    const fetchData = (): void => {
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        // Validate alert id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            return;
        }

        Promise.allSettled([
            getAlert(toNumber(params.id)),
            getAllSubscriptionGroups(),
        ])
            .then(([alertResponse, subscriptionGroupsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    alertResponse.status === "rejected" ||
                    subscriptionGroupsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertResponse.status === "fulfilled") {
                    setAlertCardData(
                        getAlertCardData(
                            alertResponse.value,
                            fetchedSubscriptionGroups
                        )
                    );
                }
            })
            .finally((): void => {
                setSubscriptionGroups(fetchedSubscriptionGroups);

                setLoading(false);
            });
    };

    const fetchVisualizationData = (): void => {
        setAlertEvaluation(null);
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!alertCardData || !alertCardData.alert) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        const timeRangeDuration = getAppTimeRangeDuration();
        getAlertEvaluation(
            createAlertEvaluation(
                alertCardData.alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        )
            .then((alertEvaluation: AlertEvaluation): void => {
                fetchedAlertEvaluation = alertEvaluation;
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setAlertEvaluation(fetchedAlertEvaluation);
            });
    };

    const onAlertStateToggle = (alertCardData: AlertCardData): void => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        // Create a copy of alert and toggle state
        const alertCopy = cloneDeep(alertCardData.alert);
        alertCopy.active = !alertCopy.active;

        // Update
        updateAlert(alertCopy)
            .then((alert: Alert): void => {
                // Replace updated alert as fetched alert
                setAlertCardData(getAlertCardData(alert, subscriptionGroups));

                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onDeleteAlert = (alertCardData: AlertCardData): void => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        // Delete
        deleteAlert(alertCardData.alert.id)
            .then((): void => {
                // Redirect to alerts all path
                history.push(getAlertsAllPath());

                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
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
                centered
                title={alertCardData ? alertCardData.name : ""}
            >
                {alertCardData && (
                    <Grid container>
                        {/* Alert */}
                        <Grid item md={12}>
                            <AlertCard
                                hideViewDetailsLinks
                                alert={alertCardData}
                                onDelete={onDeleteAlert}
                                onStateToggle={onAlertStateToggle}
                            />
                        </Grid>

                        {/* Alert evaluation time series */}
                        <Grid item md={12}>
                            <AlertEvaluationTimeSeriesCard
                                alertEvaluation={alertEvaluation}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available message */}
                {!alertCardData && <NoDataIndicator />}
            </PageContents>
        </PageContainer>
    );
};
