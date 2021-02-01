import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AlertCard } from "../../components/entity-cards/alert-card/alert-card.component";
import { AlertCardData } from "../../components/entity-cards/alert-card/alert-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    updateAlert,
} from "../../rest/alerts-rest/alerts-rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups-rest/subscription-groups-rest";
import {
    createAlertEvaluation,
    getAlertCardData,
} from "../../utils/alerts-util/alerts-util";
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
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AlertsDetailPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Fetched alert changed, set breadcrumbs
        setPageBreadcrumbs([
            {
                text: alertCardData ? alertCardData.name : "",
                onClick: (): void => {
                    if (alertCardData) {
                        history.push(getAlertsDetailPath(alertCardData.id));
                    }
                },
            },
        ]);
    }, [alertCardData]);

    useEffect(() => {
        fetchAlert();
    }, []);

    useEffect(() => {
        // Fetched alert or time range changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [alertCardData, timeRangeDuration]);

    const onAlertChange = (alertCardData: AlertCardData): void => {
        if (!alertCardData || !alertCardData.alert) {
            return;
        }

        updateAlert(alertCardData.alert)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.alert"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Replace updated alert as fetched alert
                setAlertCardData(getAlertCardData(alert, subscriptionGroups));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.alert"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onDeleteAlert = (alertCardData: AlertCardData): void => {
        if (!alertCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: alertCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAlertConfirmation(alertCardData);
            },
        });
    };

    const onDeleteAlertConfirmation = (alertCardData: AlertCardData): void => {
        if (!alertCardData) {
            return;
        }

        deleteAlert(alertCardData.id)
            .then((): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.alert"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to alerts all path
                history.push(getAlertsAllPath());
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.alert"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAlert = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

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
                let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                    setSubscriptionGroups(fetchedSubscriptionGroups);
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
                setLoading(false);
            });
    };

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!alertCardData || !alertCardData.alert) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

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
                    <Grid container direction="column">
                        {/* Alert */}
                        <Grid item md={12}>
                            <AlertCard
                                hideViewDetailsLinks
                                alertCardData={alertCardData}
                                onChange={onAlertChange}
                                onDelete={onDeleteAlert}
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
