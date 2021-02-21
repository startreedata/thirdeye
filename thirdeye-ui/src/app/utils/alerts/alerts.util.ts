import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import {
    AlertCardData,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "../../components/entity-cards/alert-card/alert-card.interfaces";
import {
    Alert,
    AlertEvaluation,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createDefaultAlert = (): Alert => {
    return {
        name: "new-alert",
        description: "This is the detection used by online service",
        nodes: {
            "detection-1": {
                type: AlertNodeType.DETECTION,
                subType: "PERCENTAGE_RULE",
                metric: {
                    name: "views",
                    dataset: {
                        name: "pageviews",
                    },
                },
                params: {
                    offset: "wo1w",
                    percentageChange: 0.2,
                } as { [index: string]: unknown },
            } as AlertNode,
        } as { [index: string]: AlertNode },
    } as Alert;
};

export const createEmptyAlertCardData = (): AlertCardData => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        active: false,
        activeText: noDataMarker,
        userId: -1,
        createdBy: noDataMarker,
        detectionTypes: [],
        filteredBy: [],
        datasetAndMetrics: [],
        subscriptionGroups: [],
        alert: null,
    };
};

export const createEmptyAlertDatasetAndMetric = (): AlertDatasetAndMetric => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        datasetId: -1,
        datasetName: noDataMarker,
        metricId: -1,
        metricName: noDataMarker,
    };
};

export const createEmptyAlertSubscriptionGroup = (): AlertSubscriptionGroup => {
    return {
        id: -1,
        name: i18n.t("label.no-data-marker"),
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
    if (isEmpty(alerts)) {
        return [];
    }

    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap = mapSubscriptionGroupsToAlertIds(
        subscriptionGroups
    );

    const alertCardDatas = [];
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
    if (isEmpty(alertCardDatas)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return alertCardDatas;
    }

    const filteredAlertCardDatas = [];
    for (const alertCardDta of alertCardDatas) {
        // Only the alert card data to be searched and not contained alert
        const alertCardDataCopy = cloneDeep(alertCardDta);
        alertCardDataCopy.alert = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    alertCardDataCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredAlertCardDatas.push(alertCardDta);

                break;
            }
        }
    }

    return filteredAlertCardDatas;
};

const getAlertCardDataInternal = (
    alert: Alert,
    subscriptionGroupsToAlertIdsMap: Map<number, AlertSubscriptionGroup[]>
): AlertCardData => {
    const alertCardData = createEmptyAlertCardData();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of alert
    alertCardData.alert = alert;

    // Basic properties
    alertCardData.id = alert.id;
    alertCardData.name = alert.name || noDataMarker;
    alertCardData.active = Boolean(alert.active);
    alertCardData.activeText = alert.active
        ? i18n.t("label.active")
        : i18n.t("label.inactive");

    // User properties
    if (alert.owner) {
        alertCardData.userId = alert.owner.id;
        alertCardData.createdBy = alert.owner.principal || noDataMarker;
    }

    // Subscription groups
    alertCardData.subscriptionGroups =
        (subscriptionGroupsToAlertIdsMap &&
            subscriptionGroupsToAlertIdsMap.get(alert.id)) ||
        [];

    // Detection, dataset and metric properties
    if (isEmpty(alert.nodes)) {
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
            continue;
        }

        const datasetAndMetric = createEmptyAlertDatasetAndMetric();
        if (alertNode.metric.dataset) {
            datasetAndMetric.datasetId = alertNode.metric.dataset.id;
            datasetAndMetric.datasetName =
                alertNode.metric.dataset.name || noDataMarker;
        }
        datasetAndMetric.metricId = alertNode.metric.id;
        datasetAndMetric.metricName = alertNode.metric.name || noDataMarker;

        alertCardData.datasetAndMetrics.push(datasetAndMetric);
    }

    return alertCardData;
};

const mapSubscriptionGroupsToAlertIds = (
    subscriptionGroups: SubscriptionGroup[]
): Map<number, AlertSubscriptionGroup[]> => {
    const subscriptionGroupsToAlertIdsMap = new Map();

    if (isEmpty(subscriptionGroups)) {
        return subscriptionGroupsToAlertIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (isEmpty(subscriptionGroup.alerts)) {
            continue;
        }

        const alertSubscriptionGroup = createEmptyAlertSubscriptionGroup();
        alertSubscriptionGroup.id = subscriptionGroup.id;
        alertSubscriptionGroup.name =
            subscriptionGroup.name || i18n.t("label.no-data-marker");

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
