/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { AxiosError } from "axios";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AlertListV1 } from "../../components/alert-list-v1/alert-list-v1.component";
import { PageHeader } from "../../components/page-header/page-header.component";
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
import { PROMISES } from "../../utils/constants/constants.util";
import { getErrorMessages } from "../../utils/rest/rest.util";

export const AlertsAllPage: FunctionComponent = () => {
    const [uiAlerts, setUiAlerts] = useState<UiAlert[] | null>(null);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch alerts
        fetchAllAlerts();
    }, []);

    const fetchAllAlerts = (): void => {
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
                    const axiosError =
                        alertsResponse.status === PROMISES.REJECTED
                            ? alertsResponse.reason
                            : subscriptionGroupsResponse.status ===
                              PROMISES.REJECTED
                            ? subscriptionGroupsResponse.reason
                            : ({} as AxiosError);

                    const errMessages = getErrorMessages(axiosError);
                    isEmpty(errMessages)
                        ? notify(
                              NotificationTypeV1.Error,
                              t("message.error-while-fetching", {
                                  entity: t(
                                      alertsResponse.status ===
                                          PROMISES.REJECTED
                                          ? "label.alerts"
                                          : "label.subscription-groups"
                                  ),
                              })
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
                          );
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
                setUiAlerts(fetchedUiAlerts);
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

            // Replace updated alert in fetched alerts
            replaceUiAlert(alert);
        });
    };

    const handleAlertDelete = (uiAlert: UiAlert): void => {
        showDialog({
            contents: t("message.delete-confirmation", {
                name: uiAlert.name,
            }),
            okButtonText: t("label.delete"),
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
        <PageV1>
            <PageHeader showCreateButton title={t("label.alerts")} />

            <PageContentsGridV1 fullHeight>
                {/* Alert list */}
                <AlertListV1
                    alerts={uiAlerts}
                    onChange={handleAlertChange}
                    onDelete={handleAlertDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
