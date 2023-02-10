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
import i18n from "i18next";
import { cloneDeep, flatten, isEmpty, uniq } from "lodash";
import { formatNumberV1 } from "../../platform/utils";
import type { Alert } from "../../rest/dto/alert.interfaces";
import type { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";
import {
    AlertAssociation,
    EmailScheme,
    SpecType,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import type {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptySubscriptionGroup = (): SubscriptionGroup => {
    return {
        name: "",
        cron: "0 */5 * * * ?",
        alerts: [] as Alert[] /** @deprecated */,
        alertAssociations: [] as AlertAssociation[],
        notificationSchemes: {
            email: {
                to: [],
            } as unknown as EmailScheme,
        },
    } as SubscriptionGroup;
};

export const createEmptyUiSubscriptionGroup = (): UiSubscriptionGroup => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
        cron: i18n.t("label.no-data-marker"),
        alerts: [],
        alertCount: formatNumberV1(0),
        dimensionCount: formatNumberV1(0),
        activeChannels: [],
        emails: [],
        emailCount: formatNumberV1(0),
        subscriptionGroup: null,
    };
};

export const createEmptyUiSubscriptionGroupAlert =
    (): UiSubscriptionGroupAlert => {
        return {
            id: -1,
            name: i18n.t("label.no-data-marker"),
        };
    };

export const getUiSubscriptionGroup = (
    subscriptionGroup: SubscriptionGroup,
    alerts: Alert[],
    enumerationItems: EnumerationItem[] = []
): UiSubscriptionGroup => {
    if (!subscriptionGroup) {
        return createEmptyUiSubscriptionGroup();
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        [subscriptionGroup],
        alerts,
        enumerationItems
    );

    return getUiSubscriptionGroupInternal(
        subscriptionGroup,
        alertsToSubscriptionGroupIdsMap
    );
};

export const getUiSubscriptionGroups = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[],
    enumerationItems: EnumerationItem[] = []
): UiSubscriptionGroup[] => {
    if (isEmpty(subscriptionGroups)) {
        return [];
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts,
        enumerationItems
    );

    const uiSubscriptionGroups = [];
    for (const subscriptionGroup of subscriptionGroups) {
        uiSubscriptionGroups.push(
            getUiSubscriptionGroupInternal(
                subscriptionGroup,
                alertsToSubscriptionGroupIdsMap
            )
        );
    }

    return uiSubscriptionGroups;
};

export const getUiSubscriptionGroupAlert = (
    alert: Alert,
    enumerationItems?: EnumerationItem[]
): UiSubscriptionGroupAlert => {
    const uiSubscriptionGroupAlert = createEmptyUiSubscriptionGroupAlert();

    if (!alert) {
        return uiSubscriptionGroupAlert;
    }

    // Basic properties
    uiSubscriptionGroupAlert.id = alert.id;
    uiSubscriptionGroupAlert.name =
        alert.name || i18n.t("label.no-data-marker");

    if (enumerationItems && enumerationItems.length > 0) {
        uiSubscriptionGroupAlert.enumerationItems = enumerationItems;
    }

    return uiSubscriptionGroupAlert;
};

export const getUiSubscriptionGroupAlerts = (
    alerts: Alert[]
): UiSubscriptionGroupAlert[] => {
    if (isEmpty(alerts)) {
        return [];
    }

    const uiSubscriptionGroupAlerts = [];
    for (const alert of alerts) {
        uiSubscriptionGroupAlerts.push(getUiSubscriptionGroupAlert(alert));
    }

    return uiSubscriptionGroupAlerts;
};

export const getUiSubscriptionGroupAlertId = (
    uiSubscriptionGroupAlert: UiSubscriptionGroupAlert
): number => {
    if (!uiSubscriptionGroupAlert) {
        return -1;
    }

    return uiSubscriptionGroupAlert.id;
};

export const getUiSubscriptionGroupAlertName = (
    uiSubscriptionGroupAlert: UiSubscriptionGroupAlert
): string => {
    if (!uiSubscriptionGroupAlert) {
        return "";
    }

    return uiSubscriptionGroupAlert.name;
};

export const filterSubscriptionGroups = (
    uiSubscriptionGroups: UiSubscriptionGroup[],
    searchWords: string[]
): UiSubscriptionGroup[] => {
    if (isEmpty(uiSubscriptionGroups)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiSubscriptionGroups;
    }

    const filteredUiSubscriptionGroups = [];
    for (const uiSubscriptionGroup of uiSubscriptionGroups) {
        // Only the UI subscription group to be searched and not contained subscription group
        const uiSubscriptionGroupCopy = cloneDeep(uiSubscriptionGroup);
        uiSubscriptionGroupCopy.subscriptionGroup = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiSubscriptionGroupCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiSubscriptionGroups.push(uiSubscriptionGroup);

                break;
            }
        }
    }

    return filteredUiSubscriptionGroups;
};

const getUiSubscriptionGroupInternal = (
    subscriptionGroup: SubscriptionGroup,
    alertsToSubscriptionGroupIdsMap: Map<number, UiSubscriptionGroupAlert[]>
): UiSubscriptionGroup => {
    const uiSubscriptionGroup = createEmptyUiSubscriptionGroup();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of subscription group
    uiSubscriptionGroup.subscriptionGroup = subscriptionGroup;

    // Basic properties
    uiSubscriptionGroup.id = subscriptionGroup.id;
    uiSubscriptionGroup.name = subscriptionGroup.name || noDataMarker;
    uiSubscriptionGroup.cron = subscriptionGroup.cron || noDataMarker;

    // Alerts
    uiSubscriptionGroup.alerts =
        (alertsToSubscriptionGroupIdsMap &&
            alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id)) ||
        [];
    uiSubscriptionGroup.alertCount = formatNumberV1(
        uiSubscriptionGroup.alerts.length
    );

    // Number of unique dimensions
    uiSubscriptionGroup.dimensionCount = uniq(
        flatten(
            uiSubscriptionGroup.alerts.map((alert) =>
                (alert?.enumerationItems || []).map((v) => v.id)
            )
        )
    ).length.toString();

    // TODO remove
    uiSubscriptionGroup.activeChannels = [
        { type: SpecType.Slack, params: { webhookUrl: "slack-url" } },
        // { type: SpecType.Webhook, params: { url: "webhook-url" } },
        // {
        //     type: SpecType.EmailSendgrid,
        //     params: {
        //         apiKey: "API-Key",
        //         emailRecipients: { from: "", to: ["rec1@gmail.com"] },
        //     },
        // },
    ];

    // Emails
    uiSubscriptionGroup.emails =
        (subscriptionGroup.notificationSchemes &&
            subscriptionGroup.notificationSchemes.email &&
            subscriptionGroup.notificationSchemes.email.to) ||
        [];
    uiSubscriptionGroup.emailCount = formatNumberV1(
        uiSubscriptionGroup.emails.length
    );

    return uiSubscriptionGroup;
};

const mapAlertsToSubscriptionGroupIds = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[],
    enumerationItems: EnumerationItem[] = []
): Map<number, UiSubscriptionGroupAlert[]> => {
    const alertsToSubscriptionGroupIdsMap = new Map<
        number,
        UiSubscriptionGroupAlert[]
    >();

    const alertItemsMap = getMapFromList(alerts);
    const enumerationItemsMap = getMapFromList(enumerationItems);

    if (isEmpty(alertItemsMap)) {
        return alertsToSubscriptionGroupIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        const alertToEnumerationItems =
            getAlertToEnumerationItemsMapForSubscriptionGroup(
                subscriptionGroup
            );

        if (isEmpty(alertToEnumerationItems)) {
            continue;
        }

        alertToEnumerationItems.forEach((enumerationItemIds, alertId) => {
            const alert = alertItemsMap.get(alertId);

            if (!alert) {
                return;
            }

            const enumerationItems = enumerationItemIds
                .map((id) => enumerationItemsMap.get(id))
                .filter(Boolean) as EnumerationItem[];

            const uiSubscriptionGroupAlert = getUiSubscriptionGroupAlert(
                alert,
                enumerationItems
            );

            const uiSubscriptionGroupAlerts =
                alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id);
            if (uiSubscriptionGroupAlerts) {
                // Add to existing list
                uiSubscriptionGroupAlerts.push(uiSubscriptionGroupAlert);
            } else {
                // Create and add to list
                alertsToSubscriptionGroupIdsMap.set(subscriptionGroup.id, [
                    uiSubscriptionGroupAlert,
                ]);
            }
        });
    }

    return alertsToSubscriptionGroupIdsMap;
};

const getAlertToEnumerationItemsMapForSubscriptionGroup = (
    subscriptionGroup: SubscriptionGroup
): Map<number, number[]> => {
    const alertToEnumerationItems = new Map<number, number[]>();
    const alertAssociations = subscriptionGroup.alertAssociations;

    if (alertAssociations && alertAssociations.length > 0) {
        alertAssociations.forEach((alertAssociation) => {
            const alertId = alertAssociation.alert.id;
            const enumerationItemId = alertAssociation.enumerationItem?.id;

            if (!alertToEnumerationItems.has(alertId)) {
                alertToEnumerationItems.set(alertId, []);
            }

            if (enumerationItemId) {
                alertToEnumerationItems.get(alertId)?.push(enumerationItemId);
            }
        });
    } else {
        // Support the legacy `alerts`
        subscriptionGroup.alerts?.forEach((alert) => {
            alertToEnumerationItems.set(alert.id, []);
        });
    }

    return alertToEnumerationItems;
};

export const getMapFromList = <T extends { id: I }, I = number>(
    list: T[]
): Map<I, T> => {
    const itemMap = new Map<I, T>();

    list.forEach((item) => {
        itemMap.set(item.id, item);
    });

    return itemMap;
};
