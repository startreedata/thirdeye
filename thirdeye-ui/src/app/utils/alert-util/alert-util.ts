import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import {
    AlertCardData,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "../../components/entity-card/alert-card/alert-card.interfaces";
import {
    Alert,
    AlertEvaluation,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { deepSearchStringProperty } from "../search-util/search-util";

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
    if (!alert) {
        return createEmptyAlertCardData();
    }

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
        alertCardDatas.push(
            getAlertCardDataInternal(alert, subscriptionGroupsToAlertIdsMap)
        );
    }

    return alertCardDatas;
};

export const filterAlerts = (
    alertCardDatas: AlertCardData[],
    searchWords: string[]
): AlertCardData[] => {
    const filteredAlertCardDatas: AlertCardData[] = [];

    if (isEmpty(alertCardDatas)) {
        // No alerts available, return empty result
        return filteredAlertCardDatas;
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original alerts
        return alertCardDatas;
    }

    for (const alert of alertCardDatas) {
        // Create a copy without original alert
        const alertCardDataCopy = cloneDeep(alert);
        alertCardDataCopy.alert = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    alertCardDataCopy,
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
                filteredAlertCardDatas.push(alert);

                break;
            }
        }
    }

    return filteredAlertCardDatas;
};

// Internal method, lacks appropriate validations
const getAlertCardDataInternal = (
    alert: Alert,
    subscriptionGroupsToAlertIdsMap: Map<number, AlertSubscriptionGroup[]>
): AlertCardData => {
    const alertCardData = createEmptyAlertCardData();

    // Maintain a copy of alert, needed when updating alert
    alertCardData.alert = alert;

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

    // Subscription groups
    alertCardData.subscriptionGroups =
        subscriptionGroupsToAlertIdsMap.get(alert.id) || [];

    // Detection, dataset and metric properties
    if (isEmpty(alert.nodes)) {
        // None available
        return alertCardData;
    }

    for (const alertNode of Object.values(alert.nodes)) {
        // Detection
        if (alertNode.type === AlertNodeType.DETECTION && alertNode.subType) {
            alertCardData.detectionTypes.push(alertNode.subType);
        } else if (
            alertNode.type === AlertNodeType.FILTER &&
            alertNode.subType
        ) {
            alertCardData.filteredBy.push(alertNode.subType);
        }

        // Dataset and metric
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

// Internal method, lacks appropriate validations
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
