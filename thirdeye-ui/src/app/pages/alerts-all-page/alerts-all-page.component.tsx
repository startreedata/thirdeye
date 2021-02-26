import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertList } from "../../components/alert-list/alert-list.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import {
    deleteAlert,
    getAllAlerts,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { getUiAlert, getUiAlerts } from "../../utils/alerts/alerts.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";

export const AlertsAllPage: FunctionComponent = () => {
    const [uiAlerts, setUiAlerts] = useState<UiAlert[] | null>([]);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch alerts
        fetchAllAlerts();
    }, [timeRangeDuration]);

    const fetchAllAlerts = (): void => {
        setUiAlerts(null);

        let fetchedUiAlerts: UiAlert[] = [];
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        Promise.allSettled([getAllAlerts(), getAllSubscriptionGroups()])
            .then(([alertsResponse, subscriptionGroupsResponse]) => {
                // Determine if any of the calls failed
                if (
                    alertsResponse.status === "rejected" ||
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
                if (alertsResponse.status === "fulfilled") {
                    fetchedUiAlerts = getUiAlerts(
                        alertsResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally(() => {
                setUiAlerts(fetchedUiAlerts);
                setSubscriptionGroups(fetchedSubscriptionGroups);
            });
    };

    const handleAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert || !uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert)
            .then((alert) => {
                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Replace updated alert in fetched alerts
                replaceUiAlert(alert);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                )
            );
    };

    const handleAlertDelete = (uiAlert: UiAlert): void => {
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
        if (!uiAlert) {
            return;
        }

        deleteAlert(uiAlert.id)
            .then((alert) => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted alert from fetched alerts
                removeUiAlert(alert);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                )
            );
    };

    const replaceUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts(
            (uiAlerts) =>
                uiAlerts &&
                uiAlerts.map((uiAlert) => {
                    if (uiAlert.id === alert.id) {
                        // Replace
                        return getUiAlert(alert, subscriptionGroups);
                    }

                    return uiAlert;
                })
        );
    };

    const removeUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts(
            (uiAlerts) =>
                uiAlerts &&
                uiAlerts.filter((uiAlert) => uiAlert.id !== alert.id)
        );
    };

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.alerts")}>
            {/* Alert list */}
            <AlertList
                uiAlerts={uiAlerts}
                onChange={handleAlertChange}
                onDelete={handleAlertDelete}
            />
        </PageContents>
    );
};
