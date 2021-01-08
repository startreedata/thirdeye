import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { deepSearchStringProperty } from "../search-util/search-util";

export const createEmptySubscriptionGroup = (): SubscriptionGroup => {
    return ({
        name: "",
        alerts: [],
        emailSettings: {
            to: [],
        },
    } as unknown) as SubscriptionGroup;
};

export const createEmptySubscriptionGroupCardData = (): SubscriptionGroupCardData => {
    const noDataAvailableMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataAvailableMarker,
        alerts: [],
        emails: [],
        subscriptionGroup: null,
    };
};

export const createEmptySubscriptionGroupAlert = (): SubscriptionGroupAlert => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
    };
};

export const getSubscriptionGroupCardData = (
    subscriptionGroup: SubscriptionGroup,
    alerts: Alert[]
): SubscriptionGroupCardData => {
    if (!subscriptionGroup) {
        return createEmptySubscriptionGroupCardData();
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        [subscriptionGroup],
        alerts
    );

    return getSubscriptionGroupCardDataInternal(
        subscriptionGroup,
        alertsToSubscriptionGroupIdsMap
    );
};

export const getSubscriptionGroupCardDatas = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): SubscriptionGroupCardData[] => {
    const subscriptionGroupCardDatas: SubscriptionGroupCardData[] = [];

    if (isEmpty(subscriptionGroups)) {
        return subscriptionGroupCardDatas;
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts
    );

    for (const subscriptionGroup of subscriptionGroups) {
        subscriptionGroupCardDatas.push(
            getSubscriptionGroupCardDataInternal(
                subscriptionGroup,
                alertsToSubscriptionGroupIdsMap
            )
        );
    }

    return subscriptionGroupCardDatas;
};

export const getSubscriptionGroupAlert = (
    alert: Alert
): SubscriptionGroupAlert => {
    const subscriptionGroupAlert = createEmptySubscriptionGroupAlert();

    if (!alert) {
        return subscriptionGroupAlert;
    }

    // Basic properties
    subscriptionGroupAlert.id = alert.id;
    subscriptionGroupAlert.name = alert.name;

    return subscriptionGroupAlert;
};

export const getSubscriptionGroupAlerts = (
    alerts: Alert[]
): SubscriptionGroupAlert[] => {
    const subscriptionGroupAlerts: SubscriptionGroupAlert[] = [];

    if (isEmpty(alerts)) {
        return subscriptionGroupAlerts;
    }

    for (const alert of alerts) {
        subscriptionGroupAlerts.push(getSubscriptionGroupAlert(alert));
    }

    return subscriptionGroupAlerts;
};

export const getSubscriptionGroupAlertId = (
    subscriptionGroupAlert: SubscriptionGroupAlert
): number => {
    if (!subscriptionGroupAlert) {
        return -1;
    }

    return subscriptionGroupAlert.id;
};

export const getSubscriptionGroupAlertName = (
    subscriptionGroupAlert: SubscriptionGroupAlert
): string => {
    if (!subscriptionGroupAlert) {
        return "";
    }

    return subscriptionGroupAlert.name;
};

export const filterSubscriptionGroups = (
    subscriptionGroupCardDatas: SubscriptionGroupCardData[],
    searchWords: string[]
): SubscriptionGroupCardData[] => {
    const filteredSubscriptionGroupCardDatas: SubscriptionGroupCardData[] = [];

    if (isEmpty(subscriptionGroupCardDatas)) {
        // No subscription groups available, return empty result
        return filteredSubscriptionGroupCardDatas;
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original subscription groups
        return subscriptionGroupCardDatas;
    }

    for (const subscriptionGroupCardData of subscriptionGroupCardDatas) {
        // Create a copy without original subscription group
        const subscriptionGroupCardDataCopy = cloneDeep(
            subscriptionGroupCardData
        );
        subscriptionGroupCardDataCopy.subscriptionGroup = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    subscriptionGroupCardDataCopy,
                    (value: string): boolean => {
                        // Check if string property value contains current search word
                        return (
                            Boolean(value) &&
                            value
                                .toLowerCase()
                                .indexOf(searchWord.toLowerCase()) > -1
                        );
                    }
                )
            ) {
                filteredSubscriptionGroupCardDatas.push(
                    subscriptionGroupCardData
                );

                break;
            }
        }
    }

    return filteredSubscriptionGroupCardDatas;
};

// Internal method, lacks appropriate validations
const getSubscriptionGroupCardDataInternal = (
    subscriptionGroup: SubscriptionGroup,
    alertsToSubscriptionGroupIdsMap: Map<number, SubscriptionGroupAlert[]>
): SubscriptionGroupCardData => {
    const subscriptionGroupCardData = createEmptySubscriptionGroupCardData();

    // Maintain a copy of subscription group, needed when updating subscription group
    subscriptionGroupCardData.subscriptionGroup = subscriptionGroup;

    // Basic properties
    subscriptionGroupCardData.id = subscriptionGroup.id;
    subscriptionGroupCardData.name = subscriptionGroup.name;

    // Alerts
    subscriptionGroupCardData.alerts =
        alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id) || [];

    // Emails
    subscriptionGroupCardData.emails =
        (subscriptionGroup.emailSettings &&
            subscriptionGroup.emailSettings.to) ||
        [];

    return subscriptionGroupCardData;
};

// Internal method, lacks appropriate validations
const mapAlertsToSubscriptionGroupIds = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): Map<number, SubscriptionGroupAlert[]> => {
    const alertsToSubscriptionGroupIdsMap = new Map<
        number,
        SubscriptionGroupAlert[]
    >();

    const alertToAlertIdsMap = mapAlertsToAlertIds(alerts);
    if (isEmpty(alertToAlertIdsMap)) {
        // No alerts available, return empty result
        return alertsToSubscriptionGroupIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (isEmpty(subscriptionGroup.alerts)) {
            // No alerts
            continue;
        }

        for (const alert of subscriptionGroup.alerts) {
            const mappedAlert = alertToAlertIdsMap.get(alert.id);
            if (!mappedAlert) {
                // Alert details not available
                continue;
            }

            const subscriptionGroupAlerts = alertsToSubscriptionGroupIdsMap.get(
                subscriptionGroup.id
            );
            if (subscriptionGroupAlerts) {
                // Add to existing list
                subscriptionGroupAlerts.push(mappedAlert);
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

// Internal method, lacks appropriate validations
const mapAlertsToAlertIds = (
    alerts: Alert[]
): Map<number, SubscriptionGroupAlert> => {
    const alertsToAlertIdsMap = new Map<number, SubscriptionGroupAlert>();

    if (isEmpty(alerts)) {
        // No alerts available, return empty result
        return alertsToAlertIdsMap;
    }

    for (const alert of alerts) {
        alertsToAlertIdsMap.set(alert.id, getSubscriptionGroupAlert(alert));
    }

    return alertsToAlertIdsMap;
};
