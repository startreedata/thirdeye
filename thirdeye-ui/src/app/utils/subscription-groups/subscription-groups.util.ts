import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { SubscriptionGroupListData } from "../../components/subscription-group-list/subscription-group-list.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    EmailScheme,
    SubscriptionGroup,
} from "../../rest/dto/subscription-group.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptySubscriptionGroup = (): SubscriptionGroup => {
    return {
        name: "",
        alerts: [] as Alert[],
        notificationSchemes: {
            email: ({
                to: [],
            } as unknown) as EmailScheme,
        },
    } as SubscriptionGroup;
};

export const createEmptySubscriptionGroupCardData = (): SubscriptionGroupCardData => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
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
    if (isEmpty(subscriptionGroups)) {
        return [];
    }

    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts
    );

    const subscriptionGroupCardDatas = [];
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
    subscriptionGroupAlert.name = alert.name || i18n.t("label.no-data-marker");

    return subscriptionGroupAlert;
};

export const getSubscriptionGroupAlerts = (
    alerts: Alert[]
): SubscriptionGroupAlert[] => {
    if (isEmpty(alerts)) {
        return [];
    }

    const subscriptionGroupAlerts = [];
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
    if (isEmpty(subscriptionGroupCardDatas)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return subscriptionGroupCardDatas;
    }

    const filteredSubscriptionGroupCardDatas = [];
    for (const subscriptionGroupCardData of subscriptionGroupCardDatas) {
        // Only the subscription group card data to be searched and not contained subscription group
        const subscriptionGroupCardDataCopy = cloneDeep(
            subscriptionGroupCardData
        );
        subscriptionGroupCardDataCopy.subscriptionGroup = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    subscriptionGroupCardDataCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
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

const getSubscriptionGroupCardDataInternal = (
    subscriptionGroup: SubscriptionGroup,
    alertsToSubscriptionGroupIdsMap: Map<number, SubscriptionGroupAlert[]>
): SubscriptionGroupCardData => {
    const subscriptionGroupCardData = createEmptySubscriptionGroupCardData();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of subscription group
    subscriptionGroupCardData.subscriptionGroup = subscriptionGroup;

    // Basic properties
    subscriptionGroupCardData.id = subscriptionGroup.id;
    subscriptionGroupCardData.name = subscriptionGroup.name || noDataMarker;

    // Alerts
    subscriptionGroupCardData.alerts =
        (alertsToSubscriptionGroupIdsMap &&
            alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id)) ||
        [];

    // Emails
    subscriptionGroupCardData.emails =
        (subscriptionGroup.notificationSchemes &&
            subscriptionGroup.notificationSchemes.email &&
            subscriptionGroup.notificationSchemes.email.to) ||
        [];

    return subscriptionGroupCardData;
};

const mapAlertsToSubscriptionGroupIds = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): Map<number, SubscriptionGroupAlert[]> => {
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

const mapAlertsToAlertIds = (
    alerts: Alert[]
): Map<number, SubscriptionGroupAlert> => {
    const alertsToAlertIdsMap = new Map();

    if (isEmpty(alerts)) {
        return alertsToAlertIdsMap;
    }

    for (const alert of alerts) {
        alertsToAlertIdsMap.set(alert.id, getSubscriptionGroupAlert(alert));
    }

    return alertsToAlertIdsMap;
};

export const createEmptySubscriptionGroupListData = (): SubscriptionGroupListData => {
    return {
        id: -1,
        idText: "",
        name: i18n.t("label.no-data-marker"),
        alerts: [],
        emails: [],
    };
};

export const getSubscriptionGroupListDataInternal = (
    subscriptionGroup: SubscriptionGroup,
    alertsToSubscriptionGroupIdsMap: Map<number, SubscriptionGroupAlert[]>
): SubscriptionGroupListData => {
    const subscriptionGroupListData = createEmptySubscriptionGroupListData();

    // Basic properties
    subscriptionGroupListData.id = subscriptionGroup.id;
    subscriptionGroupListData.idText = subscriptionGroup.id.toString();
    subscriptionGroupListData.name = subscriptionGroup.name;

    // Alerts
    subscriptionGroupListData.alerts =
        alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id) || [];

    // Emails
    subscriptionGroupListData.emails =
        (subscriptionGroup.notificationSchemes.email &&
            subscriptionGroup.notificationSchemes.email.to) ||
        [];

    return subscriptionGroupListData;
};

export const getSubscriptionGroupListDatas = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): SubscriptionGroupListData[] => {
    // Map alerts to subscription group ids
    const alertsToSubscriptionGroupIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts
    );
    const subscriptionGroupListDatas: SubscriptionGroupListData[] = [];

    if (isEmpty(subscriptionGroups)) {
        return subscriptionGroupListDatas;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        subscriptionGroupListDatas.push(
            getSubscriptionGroupListDataInternal(
                subscriptionGroup,
                alertsToSubscriptionGroupIdsMap
            )
        );
    }

    return subscriptionGroupListDatas;
};

export const filterSubscriptionGroupsList = (
    subscriptionGroupListDatas: SubscriptionGroupListData[],
    searchWords: string[]
): SubscriptionGroupListData[] => {
    const filteredSubscriptionGroupListDatas: SubscriptionGroupListData[] = [];

    if (isEmpty(subscriptionGroupListDatas)) {
        // No subscription groups List available, return empty result
        return filteredSubscriptionGroupListDatas;
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original subscription groups List
        return subscriptionGroupListDatas;
    }

    for (const subscriptionGroupTableData of subscriptionGroupListDatas) {
        // Create a copy without original subscription group
        const subscriptionGroupListDataCopy = cloneDeep(
            subscriptionGroupTableData
        );

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    subscriptionGroupListDataCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredSubscriptionGroupListDatas.push(
                    subscriptionGroupTableData
                );

                break;
            }
        }
    }

    return filteredSubscriptionGroupListDatas;
};
