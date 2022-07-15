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
import { formatNumberV1 } from "@startree-ui/platform-ui";
import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import {
    UiSubscriptionGroup,
    UiSubscriptionGroupAlert,
} from "../../rest/dto/ui-subscription-group.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptySubscriptionGroup = (): SubscriptionGroup => {
    return {
        name: "",
        cron: "",
        alerts: [] as Alert[],
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
    alerts: Alert[]
): UiSubscriptionGroup => {
    if (!subscriptionGroup) {
        return createEmptyUiSubscriptionGroup();
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        [subscriptionGroup],
        alerts
    );

    return getUiSubscriptionGroupInternal(
        subscriptionGroup,
        alertsToSubscriptionGroupIdsMap
    );
};

export const getUiSubscriptionGroups = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): UiSubscriptionGroup[] => {
    if (isEmpty(subscriptionGroups)) {
        return [];
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts
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
    alert: Alert
): UiSubscriptionGroupAlert => {
    const uiSubscriptionGroupAlert = createEmptyUiSubscriptionGroupAlert();

    if (!alert) {
        return uiSubscriptionGroupAlert;
    }

    // Basic properties
    uiSubscriptionGroupAlert.id = alert.id;
    uiSubscriptionGroupAlert.name =
        alert.name || i18n.t("label.no-data-marker");

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
    alerts: Alert[]
): Map<number, UiSubscriptionGroupAlert[]> => {
    const alertsToSubscriptionGroupIdsMap = new Map();

    const alertToAlertIdsMap = mapAlertsToAlertIds(alerts);
    if (isEmpty(alertToAlertIdsMap)) {
        return alertsToSubscriptionGroupIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (isEmpty(subscriptionGroup.alerts)) {
            continue;
        }

        for (const alert of subscriptionGroup.alerts) {
            const mappedAlert = alertToAlertIdsMap.get(alert.id);
            if (!mappedAlert) {
                continue;
            }

            const uiSubscriptionGroupAlerts =
                alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id);
            if (uiSubscriptionGroupAlerts) {
                // Add to existing list
                uiSubscriptionGroupAlerts.push(mappedAlert);
            } else {
                // Create and add to list
                alertsToSubscriptionGroupIdsMap.set(subscriptionGroup.id, [
                    mappedAlert,
                ]);
            }
        }
    }

    return alertsToSubscriptionGroupIdsMap;
};

const mapAlertsToAlertIds = (
    alerts: Alert[]
): Map<number, UiSubscriptionGroupAlert> => {
    const alertsToAlertIdsMap = new Map();

    if (isEmpty(alerts)) {
        return alertsToAlertIdsMap;
    }

    for (const alert of alerts) {
        alertsToAlertIdsMap.set(alert.id, getUiSubscriptionGroupAlert(alert));
    }

    return alertsToAlertIdsMap;
};
