/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Button, Card, CardContent, Grid, Link } from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
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
import {
    deleteAlert,
    getAlertStats,
    getAllAlerts,
} from "../../rest/alerts/alerts.rest";
import { Alert, AlertStats } from "../../rest/dto/alert.interfaces";
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
    const [alertsStats, setAlertsStats] = useState<
        Record<Alert["id"], AlertStats>
    >({});
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

    let isMounted = true;

    useEffect(() => {
        // Time range refreshed, fetch alerts
        fetchAllAlerts();
    }, []);

    useEffect(() => {
        return () => {
            isMounted = false;
        };
    }, []);

    const fetchAllAlerts = (): void => {
        setIsLoading(true);
        setUiAlerts(null);

        let fetchedUiAlerts: UiAlert[] = [];
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        Promise.allSettled([getAllAlerts(), getAllSubscriptionGroups()])
            .then(([alertsResponse, subscriptionGroupsResponse]) => {
                if (!isMounted) {
                    return;
                }
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
                if (!isMounted) {
                    return;
                }
                setIsLoading(false);
                setUiAlerts(fetchedUiAlerts);

                // Fetch the alert stats data to get the accuracy of each alert
                fetchedUiAlerts
                    .map((UiAlertItem) => UiAlertItem.id)
                    .filter((alertId) => !(alertId in alertsStats)) // Only fetch stats for those not already fetched
                    .forEach((alertId) => {
                        getAlertStats({
                            alertId,
                        }).then((alertStatsData) => {
                            if (!isMounted) {
                                return;
                            }
                            setAlertsStats((alertsStatsProp) => ({
                                ...alertsStatsProp,
                                [alertId]: alertStatsData,
                            }));
                        });
                    });
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

    // Since the alert accuracy is fetched via a different API, that data is not being injected
    // into the main uiAnomaly state to avoid adding complexity to the state updated; instead,
    // the accuracyStatistics data is stored in alertsAccuracy and injected into the uiAlerts state
    //  data asynchronously, as and when it is resolved before that is passed on to the to the
    // AlertListV1 component. This separate storage of accuracy data in this component might
    // not be especially advantageous right now, but it will be helpful if the alerts API needs
    // to be called multiple times, say for searching and filtering. Also helpful in keeping the
    // state mutation simple in cases of alert deletion.
    const uiAlertsWithAccuracy = useMemo<typeof uiAlerts>(
        () =>
            uiAlerts?.map((uiAlert) => ({
                ...uiAlert,
                accuracyStatistics: alertsStats[uiAlert.id],
            })) ?? null,
        [uiAlerts, alertsStats]
    );

    const loadingErrorStateParams = {
        isError,
        isLoading,
        wrapInGrid: true,
        wrapInCard: true,
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
                            alerts={uiAlertsWithAccuracy}
                            onAlertReset={handleAlertReset}
                            onDelete={handleAlertDelete}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
