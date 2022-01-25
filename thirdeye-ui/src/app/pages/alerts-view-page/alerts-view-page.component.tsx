import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AlertCard } from "../../components/entity-cards/alert-card/alert-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import {
    deleteAlert,
    getAlert,
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
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";

export const AlertsViewPage: FunctionComponent = () => {
    const { evaluation, getEvaluation } = useGetEvaluation();
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
    const params = useParams<AlertsViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch alert
        fetchAlert();
    }, [timeRangeDuration]);

    useEffect(() => {
        !!evaluation && setAlertEvaluation(evaluation);
    }, [evaluation]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [uiAlert]);

    const fetchAlertEvaluation = (): void => {
        if (!uiAlert || !uiAlert.alert) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(
                uiAlert.alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        );
    };

    const fetchAlert = (): void => {
        setUiAlert(null);
        let fetchedUiAlert = {} as UiAlert;
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        if (!isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                })
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

    const handleAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert).then((alert) => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", { entity: t("label.alert") })
            );

            // Replace updated alert as fetched alert
            setUiAlert(getUiAlert(alert, subscriptionGroups));
        });
    };

    const handleAlertDelete = (): void => {
        if (!uiAlert) {
            return;
        }
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAlert.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAlertDeleteOk(uiAlert),
        });
    };

    const handleAlertDeleteOk = (uiAlert: UiAlert): void => {
        deleteAlert(uiAlert.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.alert") })
            );

            // Redirect to alerts all path
            history.push(getAlertsAllPath());
        });
    };

    return !uiAlert || !alertEvaluation ? (
        <AppLoadingIndicatorV1 />
    ) : (
        <PageV1>
            <PageHeader showTimeRange title={uiAlert.name} />

            <PageContentsGridV1>
                {/* Alert Details Card*/}
                <Grid item xs={12}>
                    <AlertCard
                        alertEvaluation={alertEvaluation}
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
                        title={uiAlert.name}
                        onRefresh={fetchAlertEvaluation}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
