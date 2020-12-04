import i18n from "i18next";
import { isEmpty } from "lodash";
import {
    AlertCardData,
    AlertDatasetAndMetric,
    AlertSubscriptionGroup,
} from "../../components/alert-card/alert-card.interfaces";
import { Alert, AlertNodeType } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";

export const getAlertCardData = (
    alert: Alert,
    subscriptionGroups: SubscriptionGroup[]
): AlertCardData => {
    if (isEmpty(alert)) {
        return {} as AlertCardData;
    }

    return getAlertCardDatas([alert], subscriptionGroups)[0];
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
        const alertCardData = {} as AlertCardData;

        // Maintain a copy of alert, needed when updating/changing status of the alert
        alertCardData.alert = alert;

        // Basic properties
        alertCardData.id = alert.id;
        alertCardData.name = alert.name
            ? alert.name
            : i18n.t("label.no-data-available-marker");
        alertCardData.active = alert.active;
        alertCardData.activeText = alert.active
            ? i18n.t("label.active")
            : i18n.t("label.inactive");
        alertCardData.userId = alert.owner?.id;
        alertCardData.createdBy = alert.owner?.principal
            ? alert.owner.principal
            : i18n.t("label.no-data-available-marker");
        alertCardData.detectionTypes = [];
        alertCardData.filteredBy = [];
        alertCardData.datasetAndMetrics = [];

        if (!isEmpty(alert.nodes)) {
            for (const alertNode of Object.values(alert.nodes)) {
                // Detection properties
                if (
                    alertNode.type === AlertNodeType.DETECTION &&
                    alertNode.subType
                ) {
                    alertCardData.detectionTypes.push(alertNode.subType);
                } else if (
                    alertNode.type === AlertNodeType.FILTER &&
                    alertNode.subType
                ) {
                    alertCardData.filteredBy.push(alertNode.subType);
                }

                if (!alertNode.metric) {
                    // No metric available
                    continue;
                }

                // Dataset and metric
                const datasetAndMetric = {} as AlertDatasetAndMetric;
                if (alertNode.metric.dataset) {
                    datasetAndMetric.datasetId = alertNode.metric.dataset.id;
                    datasetAndMetric.datasetName = alertNode.metric.dataset.name
                        ? alertNode.metric.dataset.name
                        : i18n.t("label.no-data-available-marker");
                }
                datasetAndMetric.metricId = alertNode.metric.id;
                datasetAndMetric.metricName = alertNode.metric.name
                    ? alertNode.metric.name
                    : i18n.t("label.no-data-available-marker");

                alertCardData.datasetAndMetrics.push(datasetAndMetric);
            }
        }

        // Subscription groups
        alertCardData.subscriptionGroups =
            subscriptionGroupsToAlertIdsMap.get(alert.id) || [];

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
        // No anomalies available, return empty result
        return [...filteredAlerts];
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original anomalies
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
                                filteredAlerts.add(alert);
                                alertFiltered = true;

                                break;
                            }
                        }
                        // Check subscription group
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
                        // Alert already filtered, check next anomaly
                        break;
                    }
                }

                if (alertFiltered) {
                    // Alert already filtered, check next anomaly
                    break;
                }
            }

            if (alertFiltered) {
                // Alert already filtered, check next anomaly
                break;
            }
        }
    }

    return [...filteredAlerts];
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

        const alertSubscriptionGroup = {
            id: subscriptionGroup.id,
            name: subscriptionGroup.name
                ? subscriptionGroup.name
                : i18n.t("label.no-data-available-marker"),
        };

        for (const alert of subscriptionGroup.alerts) {
            const alertSubscriptionGroups = subscriptionGroupsToAlertIdsMap.get(
                alert.id
            );
            if (alertSubscriptionGroups) {
                // Add to existing list
                alertSubscriptionGroups.push(alertSubscriptionGroup);
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
