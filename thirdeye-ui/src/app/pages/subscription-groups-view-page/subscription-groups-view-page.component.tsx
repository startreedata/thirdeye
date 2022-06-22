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
import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { cloneDeep, isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { SubscriptionGroupCard } from "../../components/entity-cards/subscription-group-card/subscription-group-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { SubscriptionGroupAlertsAccordian } from "../../components/subscription-group-alerts-accordian/subscription-group-alerts-accordian.component";
import { SubscriptionGroupEmailsAccordian } from "../../components/subscription-group-emails-accordian/subscription-group-emails-accordian.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { PROMISES } from "../../utils/constants/constants.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getSubscriptionGroupsAllPath } from "../../utils/routes/routes.util";
import { getUiSubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { SubscriptionGroupsViewPageParams } from "./subscription-groups-view-page.interfaces";

export const SubscriptionGroupsViewPage: FunctionComponent = () => {
    const [uiSubscriptionGroup, setUiSubscriptionGroup] =
        useState<UiSubscriptionGroup | null>(null);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const { showDialog } = useDialogProviderV1();
    const params = useParams<SubscriptionGroupsViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch subscription group
        fetchSubscriptionGroup();
    }, []);

    const fetchSubscriptionGroup = (): void => {
        setUiSubscriptionGroup(null);
        let fetchedUiSubscriptionGroup = {} as UiSubscriptionGroup;
        let fetchedAlerts: Alert[] = [];

        if (params.id && !isValidNumberId(params.id)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                })
            );

            setUiSubscriptionGroup(fetchedUiSubscriptionGroup);
            setAlerts(fetchedAlerts);

            return;
        }

        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]) => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupResponse.status === PROMISES.REJECTED ||
                    alertsResponse.status === PROMISES.REJECTED
                ) {
                    const axiosError =
                        alertsResponse.status === PROMISES.REJECTED
                            ? alertsResponse.reason
                            : subscriptionGroupResponse.status ===
                              PROMISES.REJECTED
                            ? subscriptionGroupResponse.reason
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
                                          : "label.subscription-group"
                                  ),
                              })
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
                          );
                }

                // Attempt to gather data
                if (alertsResponse.status === PROMISES.FULFILLED) {
                    fetchedAlerts = alertsResponse.value;
                }
                if (subscriptionGroupResponse.status === PROMISES.FULFILLED) {
                    fetchedUiSubscriptionGroup = getUiSubscriptionGroup(
                        subscriptionGroupResponse.value,
                        fetchedAlerts
                    );
                }
            })
            .finally(() => {
                setUiSubscriptionGroup(fetchedUiSubscriptionGroup);
                setAlerts(fetchedAlerts);
            });
    };

    const handleSubscriptionGroupDelete = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            cancelButtonText: t("label.cancel"),
            okButtonText: t("label.delete"),
            onOk: () => handleSubscriptionGroupDeleteOk(uiSubscriptionGroup),
        });
    };

    const handleSubscriptionGroupDeleteOk = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        deleteSubscriptionGroup(uiSubscriptionGroup.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", {
                    entity: t("label.subscription-group"),
                })
            );

            // Redirect to subscription groups all path
            navigate(getSubscriptionGroupsAllPath());
        });
    };

    const handleSubscriptionGroupAlertsChange = (alerts: Alert[]): void => {
        if (!uiSubscriptionGroup || !uiSubscriptionGroup.subscriptionGroup) {
            return;
        }

        // Create a copy of subscription group and update alerts
        const subscriptionGroupCopy = cloneDeep(
            uiSubscriptionGroup.subscriptionGroup
        );
        subscriptionGroupCopy.alerts = alerts;
        saveSubscriptionGroup(subscriptionGroupCopy);
    };

    const handleSubscriptionGroupEmailsChange = (emails: string[]): void => {
        if (!uiSubscriptionGroup || !uiSubscriptionGroup.subscriptionGroup) {
            return;
        }

        // Create a copy of subscription group and update emails
        const subscriptionGroupCopy = cloneDeep(
            uiSubscriptionGroup.subscriptionGroup
        );
        if (
            subscriptionGroupCopy.notificationSchemes &&
            subscriptionGroupCopy.notificationSchemes.email
        ) {
            // Add to existing notification email scheme
            subscriptionGroupCopy.notificationSchemes.email.to = emails;
        } else if (subscriptionGroupCopy.notificationSchemes) {
            // Add to existing notification scheme
            subscriptionGroupCopy.notificationSchemes.email = {
                to: emails,
            } as EmailScheme;
        } else {
            // Create and add to notification scheme
            subscriptionGroupCopy.notificationSchemes = {
                email: {
                    to: emails,
                } as EmailScheme,
            };
        }
        saveSubscriptionGroup(subscriptionGroupCopy);
    };

    const saveSubscriptionGroup = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        updateSubscriptionGroup(subscriptionGroup).then((subscriptionGroup) => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.subscription-group"),
                })
            );

            // Replace updated subscription group as fetched subscription group
            setUiSubscriptionGroup(
                getUiSubscriptionGroup(subscriptionGroup, alerts)
            );
        });
    };

    return (
        <PageV1>
            <PageHeader
                title={uiSubscriptionGroup ? uiSubscriptionGroup.name : ""}
            />
            <PageContentsGridV1>
                {/* Subscription Group */}
                <Grid item xs={12}>
                    <SubscriptionGroupCard
                        uiSubscriptionGroup={uiSubscriptionGroup}
                        onDelete={handleSubscriptionGroupDelete}
                    />
                </Grid>

                {/* Subscribed alerts */}
                <Grid item xs={12}>
                    <SubscriptionGroupAlertsAccordian
                        alerts={alerts}
                        subscriptionGroup={uiSubscriptionGroup}
                        title={t("label.subscribe-alerts")}
                        onChange={handleSubscriptionGroupAlertsChange}
                    />
                </Grid>

                {/* Subscribed emails */}
                <Grid item xs={12}>
                    <SubscriptionGroupEmailsAccordian
                        subscriptionGroup={uiSubscriptionGroup}
                        title={t("label.subscribe-emails")}
                        onChange={handleSubscriptionGroupEmailsChange}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
