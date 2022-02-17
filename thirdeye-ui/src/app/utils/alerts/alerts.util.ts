import i18n from "i18next";
import { cloneDeep, isEmpty, omit } from "lodash";
import {
    Alert,
    AlertEvaluation,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    UiAlert,
    UiAlertDatasetAndMetric,
    UiAlertSubscriptionGroup,
} from "../../rest/dto/ui-alert.interfaces";
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

export const createEmptyUiAlert = (): UiAlert => {
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

export const createEmptyUiAlertDatasetAndMetric =
    (): UiAlertDatasetAndMetric => {
        const noDataMarker = i18n.t("label.no-data-marker");

        return {
            datasetId: -1,
            datasetName: noDataMarker,
            metricId: -1,
            metricName: noDataMarker,
        };
    };

export const createEmptyUiAlertSubscriptionGroup =
    (): UiAlertSubscriptionGroup => {
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

export const getUiAlert = (
    alert: Alert,
    subscriptionGroups: SubscriptionGroup[]
): UiAlert => {
    if (!alert) {
        return createEmptyUiAlert();
    }

    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap =
        mapSubscriptionGroupsToAlertIds(subscriptionGroups);

    return getUiAlertInternal(alert, subscriptionGroupsToAlertIdsMap);
};

export const getUiAlerts = (
    alerts: Alert[],
    subscriptionGroups: SubscriptionGroup[]
): UiAlert[] => {
    if (isEmpty(alerts)) {
        return [];
    }

    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap =
        mapSubscriptionGroupsToAlertIds(subscriptionGroups);

    const uiAlerts = [];
    for (const alert of alerts) {
        uiAlerts.push(
            getUiAlertInternal(alert, subscriptionGroupsToAlertIdsMap)
        );
    }

    return uiAlerts;
};

export const filterAlerts = (
    uiAlerts: UiAlert[],
    searchWords: string[]
): UiAlert[] => {
    if (isEmpty(uiAlerts)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiAlerts;
    }

    const filteredUiAlerts = [];
    for (const uiAlert of uiAlerts) {
        // Only the UI alert to be searched and not contained alert
        const uiAlertCopy = cloneDeep(uiAlert);
        uiAlertCopy.alert = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiAlertCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiAlerts.push(uiAlert);

                break;
            }
        }
    }

    return filteredUiAlerts;
};

export const omitNonUpdatableData = (alert: Alert): Alert => {
    const newAlert = omit(alert, "id");

    return newAlert as Alert;
};

const getUiAlertInternal = (
    alert: Alert,
    subscriptionGroupsToAlertIdsMap: Map<number, UiAlertSubscriptionGroup[]>
): UiAlert => {
    const uiAlert = createEmptyUiAlert();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of alert
    uiAlert.alert = alert;

    // Basic properties
    uiAlert.id = alert.id;
    uiAlert.name = alert.name || noDataMarker;
    uiAlert.active = Boolean(alert.active);
    uiAlert.activeText = alert.active
        ? i18n.t("label.active")
        : i18n.t("label.inactive");

    // User properties
    if (alert.owner) {
        uiAlert.userId = alert.owner.id;
        uiAlert.createdBy = alert.owner.principal || noDataMarker;
    }

    // Subscription groups
    uiAlert.subscriptionGroups =
        (subscriptionGroupsToAlertIdsMap &&
            subscriptionGroupsToAlertIdsMap.get(alert.id)) ||
        [];

    // Detection, dataset and metric properties
    if (isEmpty(alert.nodes)) {
        return uiAlert;
    }

    for (const alertNode of Object.values(alert.nodes)) {
        // Detection
        if (alertNode.type === AlertNodeType.DETECTION && alertNode.subType) {
            uiAlert.detectionTypes.push(alertNode.subType);
        } else if (
            alertNode.type === AlertNodeType.FILTER &&
            alertNode.subType
        ) {
            uiAlert.filteredBy.push(alertNode.subType);
        }

        // Dataset and metric
        if (!alertNode.metric) {
            continue;
        }

        const uiAlertDatasetAndMetric = createEmptyUiAlertDatasetAndMetric();
        if (alertNode.metric.dataset) {
            uiAlertDatasetAndMetric.datasetId = alertNode.metric.dataset.id;
            uiAlertDatasetAndMetric.datasetName =
                alertNode.metric.dataset.name || noDataMarker;
        }
        uiAlertDatasetAndMetric.metricId = alertNode.metric.id;
        uiAlertDatasetAndMetric.metricName =
            alertNode.metric.name || noDataMarker;

        uiAlert.datasetAndMetrics.push(uiAlertDatasetAndMetric);
    }

    return uiAlert;
};

const mapSubscriptionGroupsToAlertIds = (
    subscriptionGroups: SubscriptionGroup[]
): Map<number, UiAlertSubscriptionGroup[]> => {
    const subscriptionGroupsToAlertIdsMap = new Map();

    if (isEmpty(subscriptionGroups)) {
        return subscriptionGroupsToAlertIdsMap;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (isEmpty(subscriptionGroup.alerts)) {
            continue;
        }

        const uiAlertSubscriptionGroup = createEmptyUiAlertSubscriptionGroup();
        uiAlertSubscriptionGroup.id = subscriptionGroup.id;
        uiAlertSubscriptionGroup.name =
            subscriptionGroup.name || i18n.t("label.no-data-marker");

        for (const alert of subscriptionGroup.alerts) {
            const subscriptionGroups = subscriptionGroupsToAlertIdsMap.get(
                alert.id
            );
            if (subscriptionGroups) {
                // Add to existing list
                subscriptionGroups.push(uiAlertSubscriptionGroup);
            } else {
                // Create and add to list
                subscriptionGroupsToAlertIdsMap.set(alert.id, [
                    uiAlertSubscriptionGroup,
                ]);
            }
        }
    }

    return subscriptionGroupsToAlertIdsMap;
};
