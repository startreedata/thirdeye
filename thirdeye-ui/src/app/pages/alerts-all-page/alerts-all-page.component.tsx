import { Box, Button, Card, CardContent, Grid, Link } from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertListV1 } from "../../components/alert-list-v1/alert-list-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useResetAlert } from "../../rest/alerts/alerts.actions";
import { deleteAlert, getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { getUiAlerts } from "../../utils/alerts/alerts.util";
import { PROMISES } from "../../utils/constants/constants.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsCreatePath } from "../../utils/routes/routes.util";

export const AlertsAllPage: FunctionComponent = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [isError, setIsError] = useState(false);
    const [uiAlerts, setUiAlerts] = useState<UiAlert[] | null>(null);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        alert: alertThatWasReset,
        resetAlert,
        status,
        errorMessages,
    } = useResetAlert();

    // Handle communicating status to the user
    useEffect(() => {
        if (status === ActionStatus.Done && alertThatWasReset) {
            notify(
                NotificationTypeV1.Success,
                t("message.alert-reset-success", {
                    alertName: alertThatWasReset.name,
                })
            );
        }
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.alert-reset-error")
        );
    }, [status]);

    useEffect(() => {
        // Time range refreshed, fetch alerts
        fetchAllAlerts();
    }, []);

    const fetchAllAlerts = (): void => {
        setIsLoading(true);
        setUiAlerts(null);

        let fetchedUiAlerts: UiAlert[] = [];
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        Promise.allSettled([getAllAlerts(), getAllSubscriptionGroups()])
            .then(([alertsResponse, subscriptionGroupsResponse]) => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupsResponse.status === PROMISES.REJECTED ||
                    alertsResponse.status === PROMISES.REJECTED
                ) {
                    setIsError(true);
                    const axiosError =
                        alertsResponse.status === PROMISES.REJECTED
                            ? alertsResponse.reason
                            : subscriptionGroupsResponse.status ===
                              PROMISES.REJECTED
                            ? subscriptionGroupsResponse.reason
                            : ({} as AxiosError);

                    notifyIfErrors(
                        ActionStatus.Error,
                        getErrorMessages(axiosError),
                        notify,
                        t("message.error-while-fetching", {
                            entity: t(
                                alertsResponse.status === PROMISES.REJECTED
                                    ? "label.alerts"
                                    : "label.subscription-groups"
                            ),
                        })
                    );
                } else {
                    setIsError(false);
                }

                // Attempt to gather data
                if (subscriptionGroupsResponse.status === PROMISES.FULFILLED) {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertsResponse.status === PROMISES.FULFILLED) {
                    fetchedUiAlerts = getUiAlerts(
                        alertsResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally(() => {
                setIsLoading(false);
                setUiAlerts(fetchedUiAlerts);
            });
    };

    const handleAlertDelete = (uiAlert: UiAlert): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiAlert.name,
            }),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => handleAlertDeleteOk(uiAlert),
        });
    };

    const handleAlertDeleteOk = (uiAlert: UiAlert): void => {
        deleteAlert(uiAlert.id).then((alert) => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.alert") })
            );

            // Remove deleted alert from fetched alerts
            removeUiAlert(alert);
        });
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

    const handleAlertReset = (alert: Alert): void => {
        showDialog({
            type: DialogType.CUSTOM,
            contents: (
                <>
                    <p>{t("message.reset-alert-information")}</p>
                    <p>
                        {t("message.reset-alert-confirmation-prompt", {
                            alertName: alert.name,
                        })}
                    </p>
                </>
            ),
            okButtonText: t("label.confirm"),
            cancelButtonText: t("label.cancel"),
            onOk: () => {
                resetAlert(alert.id);
            },
        });
    };

    const loadingErrorStateParams = {
        isError,
        isLoading,
    };

    const emptyStateParams = {
        emptyState: (
            <Grid xs={12}>
                <Card variant="outlined">
                    <CardContent>
                        <Box pb={20} pt={20}>
                            <NoDataIndicator>
                                <Box>
                                    {t("message.no-alerts-created")}{" "}
                                    <Link
                                        href="https://dev.startree.ai/docs/startree-enterprise-edition/startree-thirdeye/getting-started/create-your-first-alert"
                                        target="_blank"
                                    >
                                        {t("message.view-documentation")}
                                    </Link>{" "}
                                    {t("message.on-how-to-create-entity", {
                                        entity: t("label.alert"),
                                    })}
                                </Box>
                                <Box marginTop={2} textAlign="center">
                                    or
                                </Box>
                                <Box marginTop={2} textAlign="center">
                                    <Button
                                        color="primary"
                                        href={getAlertsCreatePath()}
                                    >
                                        {t("label.create-an-entity", {
                                            entity: t("label.alert"),
                                        })}
                                    </Button>
                                </Box>
                            </NoDataIndicator>
                        </Box>
                    </CardContent>
                </Card>
            </Grid>
        ),
        isEmpty: !!uiAlerts && uiAlerts.length === 0,
    };

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                transparentBackground
                title={t("label.alerts")}
            />

            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch {...loadingErrorStateParams}>
                    <EmptyStateSwitch {...emptyStateParams}>
                        <AlertListV1
                            alerts={uiAlerts}
                            onAlertReset={handleAlertReset}
                            onDelete={handleAlertDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
