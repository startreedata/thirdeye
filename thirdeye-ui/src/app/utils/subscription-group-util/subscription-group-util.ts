import i18n from "i18next";
import { isEmpty } from "lodash";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/subscription-group-card/subscription-group.interfaces";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

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

            // Try and match alert property values to search words
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
                        // Check dataset and metric
                        else if (arrayValue.datasetId) {
                            if (
                                arrayValue.datasetName
                                    .toLowerCase()
                                    .indexOf(searchWord.toLowerCase()) > -1 ||
                                arrayValue.metricName
                                    .toLowerCase()
                                    .indexOf(searchWord.toLowerCase()) > -1
                            ) {
                                filteredSubscriptionGroups.add(
                                    subscriptionGroup
                                );
                                subscriptionGroupFiltered = true;

                                break;
                            }
                        }
                        // Check alert
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
                        // Subscription group already filtered, check next Subscription group
                        break;
                    }
                }

                if (subscriptionGroupFiltered) {
                    // Subscription group already filtered, check next Subscription group
                    break;
                }
            }

            if (subscriptionGroupFiltered) {
                // Subscription group already filtered, check next Subscription group
                break;
            }
        }
    }

    return [...filteredSubscriptionGroups];
};

export const getSubscriptionGroupCardData = (
    subscriptionGroup: SubscriptionGroup,
    alerts: Alert[]
): SubscriptionGroupCardData => {
    if (isEmpty(subscriptionGroup)) {
        return {} as SubscriptionGroupCardData;
    }

    return getSubscriptionGroupCardDatas([subscriptionGroup], alerts)[0];
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
    const subscriptionGroupsToAlertIdsMap = mapAlertsToSubscriptionGroupIds(
        subscriptionGroups,
        alerts
    );

    for (const subscriptionGroup of subscriptionGroups) {
        const subscriptionGroupCardData = {} as SubscriptionGroupCardData;

        // Maintain a copy of subscriptionGroup
        subscriptionGroupCardData.subscriptionGroup = subscriptionGroup;

        // Basic properties
        subscriptionGroupCardData.id = subscriptionGroup.id;
        subscriptionGroupCardData.name = subscriptionGroup.name
            ? subscriptionGroup.name
            : i18n.t("label.no-data-available-marker");

        subscriptionGroupCardData.application =
            subscriptionGroup.application.name;
        subscriptionGroupCardData.alerts =
            subscriptionGroupsToAlertIdsMap.get(subscriptionGroup.id) || [];
        subscriptionGroupCardData.created = subscriptionGroup.created;
        subscriptionGroupCardData.updated = subscriptionGroup.updated;
        subscriptionGroupCardData.emails =
            subscriptionGroup.emailSettings?.to || [];

        subscriptionGroupCardDatas.push(subscriptionGroupCardData);
    }

    return subscriptionGroupCardDatas;
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

    for (const subscriptionGroup of subscriptionGroups) {
        const alertsMap = [] as SubscriptionGroupAlert[];

        for (const alert of subscriptionGroup?.alerts || []) {
            const alertData = alerts.find((alrt) => alrt.id === alert.id);

            alertsMap.push({
                id: alert.id,
                name: alertData?.name
                    ? alertData.name
                    : i18n.t("label.no-data-available-marker"),
            });
        }

        // Create and add to list
        alertsToSubscriptionGroupIdsMap.set(subscriptionGroup.id, alertsMap);
    }

    return alertsToSubscriptionGroupIdsMap;
};
