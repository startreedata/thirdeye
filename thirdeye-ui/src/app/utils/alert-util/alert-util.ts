import i18n from "i18next";
import { isEmpty } from "lodash";
import {
    AlertCardData,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "../../components/alert-card/alert-card.interfaces";
import {
    Alert,
    AlertEvaluation,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export const createEmptyAlertCardData = (): AlertCardData => {
    const noDataAvailableMarker = i18n.t("label.no-data-available-marker");

    return {
        id: -1,
        name: noDataAvailableMarker,
        active: false,
        activeText: noDataAvailableMarker,
        userId: -1,
        createdBy: noDataAvailableMarker,
        detectionTypes: [],
        filteredBy: [],
        datasetAndMetrics: [],
        subscriptionGroups: [],
        alert: null,
    };
};

export const createEmptyAlertDatasetAndMetric = (): AlertDatasetAndMetric => {
    const noDataAvailableMarker = i18n.t("label.no-data-available-marker");

    return {
        datasetId: -1,
        datasetName: noDataAvailableMarker,
        metricId: -1,
        metricName: noDataAvailableMarker,
    };
};

export const createEmptyAlertSubscriptionGroup = (): AlertSubscriptionGroup => {
    const noDataAvailableMarker = i18n.t("label.no-data-available-marker");

    return {
        id: -1,
        name: noDataAvailableMarker,
    };
};

export const createAlertEvaluation = (
    alert: Alert,
    startTime: number,
    endTime: number
): AlertEvaluation => {
    return {
        alert: alert,
        start: startTime,
        end: endTime,
    } as AlertEvaluation;
};

export const getAlertCardData = (
    alert: Alert,
    subscriptionGroups: SubscriptionGroup[]
): AlertCardData => {
    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap = mapSubscriptionGroupsToAlertIds(
        subscriptionGroups
    );

    return getAlertCardDataInternal(alert, subscriptionGroupsToAlertIdsMap);
};

export const getAlertCardDatas = (
    alerts: Alert[],
    subscriptionGroups: SubscriptionGroup[]
): AlertCardData[] => {
    const alertCardDatas: AlertCardData[] = [];

    if (isEmpty(alerts)) {
        return alertCardDatas;
    }

    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap = mapSubscriptionGroupsToAlertIds(
        subscriptionGroups
    );

    for (const alert of alerts) {
        const alertCardData = getAlertCardDataInternal(
            alert,
            subscriptionGroupsToAlertIdsMap
        );
        alertCardDatas.push(alertCardData);
    }

    return alertCardDatas;
};

export const filterAlerts = (
    alerts: AlertCardData[],
    searchWords: string[]
): AlertCardData[] => {
    const filteredAlerts = new Set<AlertCardData>();

    if (isEmpty(alerts)) {
        // No alerts available, return empty result
        return [...filteredAlerts];
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original alerts
        return alerts;
    }

    for (const alert of alerts) {
        for (const searchWord of searchWords) {
            let alertFiltered = false;

            // Try and match alert property values to search words
            for (const propertyValue of Object.values(alert)) {
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
                    filteredAlerts.add(alert);
                    alertFiltered = true;

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
                            filteredAlerts.add(alert);
                            alertFiltered = true;

                            break;
                        }

                        // Check dataset and metric values
                        else if (arrayValue.datasetId) {
                            if (
                                arrayValue.datasetName
                                    .toLowerCase()
                                    .indexOf(searchWord.toLowerCase()) > -1 ||
                                arrayValue.metricName
                                    .toLowerCase()
                                    .indexOf(searchWord.toLowerCase()) > -1
                            ) {
                                filteredAlerts.add(alert);
                                alertFiltered = true;

                                break;
                            }
                        }

                        // Check subscription group value
                        else if (
                            arrayValue.name &&
                            arrayValue.name
                                .toLowerCase()
                                .indexOf(searchWord.toLowerCase()) > -1
                        ) {
                            filteredAlerts.add(alert);
                            alertFiltered = true;

                            break;
                        }
                    }

                    if (alertFiltered) {
                        // Alert already filtered, check next alert
                        break;
                    }
                }

                if (alertFiltered) {
                    // Alert already filtered, check next alert
                    break;
                }
            }

            if (alertFiltered) {
                // Alert already filtered, check next alert
                break;
            }
        }
    }

    return [...filteredAlerts];
};

const getAlertCardDataInternal = (
    alert: Alert,
    subscriptionGroupsToAlertIdsMap: Map<number, AlertSubscriptionGroup[]>
): AlertCardData => {
    const alertCardData = createEmptyAlertCardData();

    if (!alert) {
        return alertCardData;
    }

    // Basic properties
    alertCardData.id = alert.id;
    alertCardData.name = alert.name;
    alertCardData.active = alert.active;
    alertCardData.activeText = alert.active
        ? i18n.t("label.active")
        : i18n.t("label.inactive");

    // User properties
    if (alert.owner) {
        alertCardData.userId = alert.owner.id;
        alertCardData.createdBy = alert.owner.principal;
    }

    // Maintain a copy of alert, needed when updating/changing status of the alert
    alertCardData.alert = alert;

    // Subscription groups
    if (!isEmpty(subscriptionGroupsToAlertIdsMap)) {
        alertCardData.subscriptionGroups =
            subscriptionGroupsToAlertIdsMap.get(alert.id) || [];
    }

    // Detection, dataset and metric properties
    if (isEmpty(alert.nodes)) {
        // None available
        return alertCardData;
    }

    for (const alertNode of Object.values(alert.nodes)) {
        // Detection properties
        if (alertNode.type === AlertNodeType.DETECTION && alertNode.subType) {
            alertCardData.detectionTypes.push(alertNode.subType);
        } else if (
            alertNode.type === AlertNodeType.FILTER &&
            alertNode.subType
        ) {
            alertCardData.filteredBy.push(alertNode.subType);
        }

        // Dataset and metric properties
        if (!alertNode.metric) {
            // None available
            continue;
        }

        const datasetAndMetric = createEmptyAlertDatasetAndMetric();
        if (alertNode.metric.dataset) {
            datasetAndMetric.datasetId = alertNode.metric.dataset.id;
            datasetAndMetric.datasetName = alertNode.metric.dataset.name;
        }
        datasetAndMetric.metricId = alertNode.metric.id;
        datasetAndMetric.metricName = alertNode.metric.name;

        alertCardData.datasetAndMetrics.push(datasetAndMetric);
    }

    return alertCardData;
};

const mapSubscriptionGroupsToAlertIds = (
    subscriptionGroups: SubscriptionGroup[]
): Map<number, AlertSubscriptionGroup[]> => {
    const subscriptionGroupsToAlertIdsMap = new Map<
        number,
        AlertSubscriptionGroup[]
    >();

    if (isEmpty(subscriptionGroups)) {
        // No subscription groups available, return empty result
        return subscriptionGroupsToAlertIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (isEmpty(subscriptionGroup.alerts)) {
            // No alerts
            continue;
        }

        const alertSubscriptionGroup = createEmptyAlertSubscriptionGroup();
        alertSubscriptionGroup.id = subscriptionGroup.id;
        alertSubscriptionGroup.name = subscriptionGroup.name;

        for (const alert of subscriptionGroup.alerts) {
            const subscriptionGroups = subscriptionGroupsToAlertIdsMap.get(
                alert.id
            );
            if (subscriptionGroups) {
                // Add to existing list
                subscriptionGroups.push(alertSubscriptionGroup);
            } else {
                // Create and add to list
                subscriptionGroupsToAlertIdsMap.set(alert.id, [
                    alertSubscriptionGroup,
                ]);
            }
        }
    }

    return subscriptionGroupsToAlertIdsMap;
};
