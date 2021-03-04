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
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import {
    createAlertEvaluation,
    getUiAlert,
} from "../../utils/alerts/alerts.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAlertsAllPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { AlertsDetailPageParams } from "./alerts-detail-page.interfaces";

export const AlertsDetailPage: FunctionComponent = () => {
    const [uiAlert, setUiAlert] = useState<UiAlert | null>(null);
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
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch alert
        fetchAlert();
    }, [timeRangeDuration]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [uiAlert]);

    const fetchAlert = (): void => {
        setUiAlert(null);
        let fetchedUiAlert = {} as UiAlert;
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        if (!isValidNumberId(params.id)) {
            // Invalid id
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            setUiAlert(fetchedUiAlert);
            setSubscriptionGroups(fetchedSubscriptionGroups);

            return;
        }

        Promise.allSettled([
            getAlert(toNumber(params.id)),
            getAllSubscriptionGroups(),
        ])
            .then(([alertResponse, subscriptionGroupsResponse]) => {
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
                    fetchedUiAlert = getUiAlert(
                        alertResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally(() => {
                setUiAlert(fetchedUiAlert);
                setSubscriptionGroups(fetchedSubscriptionGroups);
            });
    };

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!uiAlert || !uiAlert.alert) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        getAlertEvaluation(
            createAlertEvaluation(
                uiAlert.alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        )
            .then((alertEvaluation) => {
                fetchedAlertEvaluation = alertEvaluation;
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                )
            )
            .finally(() => setAlertEvaluation(fetchedAlertEvaluation));
    };

    const handleAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert)
            .then((alert) => {
                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Replace updated alert as fetched alert
                setUiAlert(getUiAlert(alert, subscriptionGroups));
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                )
            );
    };

    const handleAlertDelete = (uiAlert: UiAlert): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAlert.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAlertDeleteOk(uiAlert),
        });
    };

    const handleAlertDeleteOk = (uiAlert: UiAlert): void => {
        deleteAlert(uiAlert.id)
            .then(() => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Redirect to alerts all path
                history.push(getAlertsAllPath());
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                )
            );
    };

    return (
        <PageContents centered title={uiAlert ? uiAlert.name : ""}>
            <Grid container>
                {/* Alert */}
                <Grid item xs={12}>
                    <AlertCard
                        uiAlert={uiAlert}
                        onChange={handleAlertChange}
                        onDelete={handleAlertDelete}
                    />
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    <AlertEvaluationTimeSeriesCard
                        alertEvaluation={alertEvaluation}
                        alertEvaluationTimeSeriesHeight={500}
                        maximizedTitle={uiAlert ? uiAlert.name : ""}
                        onRefresh={fetchAlertEvaluation}
                    />
                </Grid>
            </Grid>
        </PageContents>
    );
};
