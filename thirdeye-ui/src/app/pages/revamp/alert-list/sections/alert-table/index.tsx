/*
 * Copyright 2025 StarTree Inc
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
import React, { useEffect, useState } from "react";
import {
    NotificationTypeV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../../../../platform/components";
import { Alert } from "../../../../../rest/dto/alert.interfaces";
import { DialogType } from "../../../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { useTranslation } from "react-i18next";
// import { useResetAlert } from "../../../../../rest/alerts/alerts.actions";
import { deleteAlert } from "../../../../../rest/alerts/alerts.rest";
import { UiAlert } from "../../../../../rest/dto/ui-alert.interfaces";
import { EmptyStateView } from "./empty-state";
import { EmptyStateSwitch } from "../../../../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../../../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { getUiAlerts } from "../../../../../utils/alerts/alerts.util";
// import { AlertListV1 } from "../../../../../components/alert-list-v1/alert-list-v1.component";
import { useAlertApiRequest } from "./api/useAlertApiRequests";
import { AlertList } from "../../../../../components/revamp/alert/alert-list";
// import { ActionStatus } from "../../../../../rest/actions.interfaces";
// import { notifyIfErrors } from "../../../../../utils/notifications/notifications.util";

export const AlertTable = (): JSX.Element => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const { notify } = useNotificationProviderV1();
    const [uiAlerts, setUiAlerts] = useState<UiAlert[]>([]);

    const { alerts, subscriptionGroups, error, loading, resetAlert } =
        useAlertApiRequest();

    useEffect(() => {
        if (alerts) {
            if (subscriptionGroups) {
                setUiAlerts(getUiAlerts(alerts, subscriptionGroups));
            } else {
                setUiAlerts(getUiAlerts(alerts, []));
            }
        }
    }, [alerts, subscriptionGroups]);

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
            dataTestId: "reset-alert-dialog",
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
            dataTestId: "delete-alert-dialog",
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

    const loadingErrorStateParams = {
        isError: error,
        isLoading: loading,
        wrapInGrid: true,
        wrapInCard: true,
    };

    const emptyStateParams = {
        emptyState: EmptyStateView,
        isEmpty: !!uiAlerts && uiAlerts.length === 0,
    };

    return (
        <LoadingErrorStateSwitch {...loadingErrorStateParams}>
            <EmptyStateSwitch {...emptyStateParams}>
                <AlertList
                    alerts={uiAlerts}
                    onAlertReset={handleAlertReset}
                    onDelete={handleAlertDelete}
                />
            </EmptyStateSwitch>
        </LoadingErrorStateSwitch>
    );
};
