import i18n from "i18next";
import { isEmpty } from "lodash";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/subscription-group-card/subscription-group-card.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export const createEmptySubscriptionGroupCardData = (): SubscriptionGroupCardData => {
    const noDataAvailableMarker = i18n.t("label.no-data-available-marker");

    return {
        id: -1,
        name: noDataAvailableMarker,
        application: noDataAvailableMarker,
        alerts: [],
        emails: [],
        subscriptionGroup: null,
    };
};

export const createEmptySubscriptionGroupAlert = (): SubscriptionGroupAlert => {
    return {
        id: -1,
        name: i18n.t("label.no-data-available-marker"),
    };
};

export const getSubscriptionGroupCardData = (
    subscriptionGroup: SubscriptionGroup,
    alerts: Alert[]
): SubscriptionGroupCardData => {
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
        const subscriptionGroupCardData = getSubscriptionGroupCardDataInternal(
            subscriptionGroup,
            alertsToSubscriptionGroupIdsMap
        );
        subscriptionGroupCardDatas.push(subscriptionGroupCardData);
    }

    return subscriptionGroupCardDatas;
};

export const filterSubscriptionGroups = (
    subscriptionGroups: SubscriptionGroupCardData[],
    searchWords: string[]
): SubscriptionGroupCardData[] => {
    const filteredSubscriptionGroups = new Set<SubscriptionGroupCardData>();

    if (isEmpty(subscriptionGroups)) {
        // No subscription groups available, return empty result
        return [...filteredSubscriptionGroups];
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original subscription groups
        return subscriptionGroups;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        for (const searchWord of searchWords) {
            let subscriptionGroupFiltered = false;

            // Try and match subscription group property values to search words
            for (const propertyValue of Object.values(subscriptionGroup)) {
                if (!propertyValue) {
                    continue;
                }

                // Check basic string value
                if (
                    typeof propertyValue === "string" &&
                    propertyValue
                        .toLowerCase()
                        .indexOf(searchWord.toLowerCase()) > -1
                ) {
                    filteredSubscriptionGroups.add(subscriptionGroup);
                    subscriptionGroupFiltered = true;

                    break;
                }

                // Check arrays
                else if (propertyValue.length && propertyValue.length > 0) {
                    for (const arrayValue of propertyValue) {
                        if (!arrayValue) {
                            continue;
                        }

                        // Check basic string value
                        if (
                            typeof arrayValue === "string" &&
                            arrayValue
                                .toLowerCase()
                                .indexOf(searchWord.toLowerCase()) > -1
                        ) {
                            filteredSubscriptionGroups.add(subscriptionGroup);
                            subscriptionGroupFiltered = true;

                            break;
                        }

                        // Check alert value
                        else if (
                            arrayValue.name &&
                            arrayValue.name
                                .toLowerCase()
                                .indexOf(searchWord.toLowerCase()) > -1
                        ) {
                            filteredSubscriptionGroups.add(subscriptionGroup);
                            subscriptionGroupFiltered = true;

                            break;
                        }
                    }

                    if (subscriptionGroupFiltered) {
                        // Subscription group already filtered, check next subscription group
                        break;
                    }
                }

                if (subscriptionGroupFiltered) {
                    // Subscription group already filtered, check next subscription group
                    break;
                }
            }

            if (subscriptionGroupFiltered) {
                // Subscription group already filtered, check next subscription group
                break;
            }
        }
    }

    return [...filteredSubscriptionGroups];
};

const getSubscriptionGroupCardDataInternal = (
    subscriptionGroup: SubscriptionGroup,
    alertsToSubscriptionGroupIdsMap: Map<number, SubscriptionGroupAlert[]>
): SubscriptionGroupCardData => {
    const subscriptionGroupCardData = createEmptySubscriptionGroupCardData();

    if (!subscriptionGroup) {
        return subscriptionGroupCardData;
    }

    // Basic properties
    subscriptionGroupCardData.id = subscriptionGroup.id;
    subscriptionGroupCardData.name = subscriptionGroup.name;

    // Application properties
    if (subscriptionGroup.application) {
        subscriptionGroupCardData.application =
            subscriptionGroup.application.name;
    }

    // Maintain a copy of subscription group, needed when updating/changing subscription group
    subscriptionGroupCardData.subscriptionGroup = subscriptionGroup;

    // Alerts
    if (!isEmpty(alertsToSubscriptionGroupIdsMap)) {
        subscriptionGroupCardData.alerts =
            alertsToSubscriptionGroupIdsMap.get(subscriptionGroup.id) || [];
    }

    // Emails
    if (
        subscriptionGroup.emailSettings &&
        !isEmpty(subscriptionGroup.emailSettings.to)
    ) {
        subscriptionGroupCardData.emails = subscriptionGroup.emailSettings.to;
    }

    return subscriptionGroupCardData;
};

const mapAlertsToSubscriptionGroupIds = (
    subscriptionGroups: SubscriptionGroup[],
    alerts: Alert[]
): Map<number, SubscriptionGroupAlert[]> => {
    const alertsToSubscriptionGroupIdsMap = new Map<
        number,
        SubscriptionGroupAlert[]
    >();

    if (isEmpty(subscriptionGroups)) {
        // No subscription groups available, return empty result
        return alertsToSubscriptionGroupIdsMap;
    }

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
                // Alert not available
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
    const alertsToAlertIdsMap = new Map<number, SubscriptionGroupAlert>();

    if (isEmpty(alerts)) {
        // No alerts available, return empty result
        return alertsToAlertIdsMap;
    }

    for (const alert of alerts) {
        const subscriptionGroupAlert = createEmptySubscriptionGroupAlert();
        subscriptionGroupAlert.id = alert.id;
        subscriptionGroupAlert.name = alert.name;

        alertsToAlertIdsMap.set(alert.id, subscriptionGroupAlert);
    }

    return alertsToAlertIdsMap;
};
