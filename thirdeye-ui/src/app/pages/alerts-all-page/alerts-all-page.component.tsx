import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertList } from "../../components/alert-list/alert-list.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
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
    const [loading, setLoading] = useState(true);
    const [uiAlerts, setUiAlerts] = useState<UiAlert[]>([]);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllAlerts();
    }, []);

    const onAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert || !uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Replace updated alert in fetched alerts
                replaceUiAlert(alert);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onDeleteAlert = (uiAlert: UiAlert): void => {
        if (!uiAlert) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiAlert.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAlertConfirmation(uiAlert);
            },
        });
    };

    const onDeleteAlertConfirmation = (uiAlert: UiAlert): void => {
        if (!uiAlert) {
            return;
        }

        deleteAlert(uiAlert.id)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.delete-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted alert from fetched alerts
                removeUiAlert(alert);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAllAlerts = (): void => {
        Promise.allSettled([getAllAlerts(), getAllSubscriptionGroups()])
            .then(([alertsResponse, subscriptionGroupsResponse]): void => {
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
                let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                    setSubscriptionGroups(fetchedSubscriptionGroups);
                }
                if (alertsResponse.status === "fulfilled") {
                    setUiAlerts(
                        getUiAlerts(
                            alertsResponse.value,
                            fetchedSubscriptionGroups
                        )
                    );
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const replaceUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts((uiAlerts) =>
            uiAlerts.map(
                (uiAlert: UiAlert): UiAlert => {
                    if (uiAlert.id === alert.id) {
                        // Replace
                        return getUiAlert(alert, subscriptionGroups);
                    }

                    return uiAlert;
                }
            )
        );
    };

    const removeUiAlert = (alert: Alert): void => {
        if (!alert) {
            return;
        }

        setUiAlerts((uiAlerts) =>
            uiAlerts.filter((uiAlert: UiAlert): boolean => {
                return uiAlert.id !== alert.id;
            })
        );
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.alerts")}>
            <AlertList
                uiAlerts={uiAlerts}
                onChange={onAlertChange}
                onDelete={onDeleteAlert}
            />
        </PageContents>
    );
};
