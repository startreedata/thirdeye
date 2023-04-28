/*
 * Copyright 2023 StarTree Inc
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
import { Icon } from "@iconify/react";
import { Box, Button, Card, CardContent, Grid, Link } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { AlertListV1 } from "../../components/alert-list-v1/alert-list-v1.component";
import { alertsBasicHelpCards } from "../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAlerts, useResetAlert } from "../../rest/alerts/alerts.actions";
import { deleteAlert } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { useGetSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.actions";
import { getUiAlerts } from "../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getAlertsCreatePath } from "../../utils/routes/routes.util";

export const AlertsAllPage: FunctionComponent = () => {
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
    const {
        alerts,
        getAlerts,
        status: getAlertsStatus,
        errorMessages: getAlertsErrors,
    } = useGetAlerts();
    const {
        subscriptionGroups,
        getSubscriptionGroups,
        status: getSubscriptionGroupStatus,
        errorMessages: getSubscriptionGroupErrors,
    } = useGetSubscriptionGroups();

    // Handle communicating alert reset status to the user
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
        notifyIfErrors(
            getAlertsStatus,
            getAlertsErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alerts"),
            })
        );
    }, [getAlertsErrors]);

    useEffect(() => {
        notifyIfErrors(
            getSubscriptionGroupStatus,
            getSubscriptionGroupErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.subscription-groups"),
            })
        );
    }, [getSubscriptionGroupErrors]);

    useEffect(() => {
        getAlerts();
        getSubscriptionGroups();
    }, []);

    useEffect(() => {
        if (alerts) {
            if (subscriptionGroups) {
                setUiAlerts(getUiAlerts(alerts, subscriptionGroups));
            } else {
                setUiAlerts(getUiAlerts(alerts, []));
            }
        }
    }, [alerts, subscriptionGroups]);

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
        isError: getAlertsStatus === ActionStatus.Error,
        isLoading:
            getAlertsStatus === ActionStatus.Working ||
            getAlertsStatus === ActionStatus.Initial ||
            getSubscriptionGroupStatus === ActionStatus.Initial ||
            getSubscriptionGroupStatus === ActionStatus.Working,
        wrapInGrid: true,
        wrapInCard: true,
    };

    const emptyStateParams = {
        emptyState: (
            <Grid item xs={12}>
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
                transparentBackground
                customActions={
                    <PageHeaderActionsV1>
                        <HelpDrawerV1
                            cards={alertsBasicHelpCards}
                            title={`${t("label.need-help")}?`}
                            trigger={(handleOpen) => (
                                <Button
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                    onClick={handleOpen}
                                >
                                    <Box component="span" mr={1}>
                                        {t("label.need-help")}
                                    </Box>
                                    <Box component="span" display="flex">
                                        <Icon
                                            fontSize={24}
                                            icon="mdi:question-mark-circle-outline"
                                        />
                                    </Box>
                                </Button>
                            )}
                        />
                        <Button
                            color="primary"
                            component={RouterLink}
                            to={getAlertsCreatePath()}
                        >
                            {t("label.create")}
                        </Button>
                    </PageHeaderActionsV1>
                }
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
