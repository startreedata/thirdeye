import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { cloneDeep, isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { SubscriptionGroupCard } from "../../components/entity-cards/subscription-group-card/subscription-group-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { SubscriptionGroupAlertsAccordian } from "../../components/subscription-group-alerts-accordian/subscription-group-alerts-accordian.component";
import { SubscriptionGroupEmailsAccordian } from "../../components/subscription-group-emails-accordian/subscription-group-emails-accordian.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
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
    const { showDialog } = useDialog();
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
                              t("message.fetch-error")
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
            text: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            okButtonLabel: t("label.delete"),
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
