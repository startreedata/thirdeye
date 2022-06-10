///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


import i18n from "i18next";
import { cloneDeep, isEmpty, omit, sortBy } from "lodash";
import {
    Alert,
    AlertAnomalyDetectorNode,
    AlertEvaluation,
    AlertNodeType,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    UiAlert,
    UiAlertDatasetAndMetric,
    UiAlertSubscriptionGroup,
} from "../../rest/dto/ui-alert.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

// fixme cyril update this template
export const createDefaultAlert = (): EditableAlert => {
    return {
        name: "simple-threshold-template",
        description:
            "Sample threshold alert. Runs every hour. Change the template properties to run on your data",
        cron: "0 0 0 1/1 * ? *",
        template: {
            nodes: [
                {
                    name: "root",
                    type: "AnomalyDetector",
                    params: {
                        type: "THRESHOLD",
                        "component.timezone": "UTC",
                        "component.monitoringGranularity":
                            "${monitoringGranularity}",
                        "component.timestamp": "ts",
                        "component.metric": "met",
                        "component.max": "${max}",
                        "component.min": "${min}",
                        "anomaly.metric": "${aggregateFunction}(${metric})",
                    },
                    inputs: [
                        {
                            targetProperty: "current",
                            sourcePlanNode: "currentDataFetcher",
                            sourceProperty: "currentData",
                        },
                    ],
                },
                {
                    name: "currentDataFetcher",
                    type: "DataFetcher",
                    params: {
                        "component.dataSource": "${dataSource}",
                        "component.query":
                            "SELECT __timeGroup(\"${timeColumn}\", '${timeColumnFormat}'," +
                            " '${monitoringGranularity}') as ts, ${aggregateFunction}(${metric}) as met FROM " +
                            "${dataset} WHERE __timeFilter(\"${timeColumn}\", '${timeColumnFormat}') GROUP BY ts ORDER BY ts LIMIT 1000",
                    },
                    outputs: [
                        {
                            outputKey: "pinot",
                            outputName: "currentData",
                        },
                    ],
                },
            ],
            metadata: {
                datasource: {
                    name: "${dataSource}",
                },
                dataset: {
                    name: "${dataset}",
                },
                metric: {
                    name: "${metric}",
                },
            },
        },
        templateProperties: {
            dataSource: "pinotQuickStart",
            dataset: "pageviews",
            aggregateFunction: "sum",
            metric: "views",
            monitoringGranularity: "P1D",
            timeColumn: "date",
            timeColumnFormat: "yyyyMMdd",
            max: "850000",
            min: "250000",
        },
    };
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
        datasetAndMetrics: [],
        subscriptionGroups: [],
        renderedMetadata: [],
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
    alert: Alert | EditableAlert,
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
    alert: EditableAlert,
    subscriptionGroups: SubscriptionGroup[]
): UiAlert => {
    if (!alert) {
        return createEmptyUiAlert();
    }

    // Map subscription groups to alert ids
    const subscriptionGroupsToAlertIdsMap =
        mapSubscriptionGroupsToAlertIds(subscriptionGroups);

    return getUiAlertInternal(alert as Alert, subscriptionGroupsToAlertIdsMap);
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

export const omitNonUpdatableData = (
    alert: Alert | EditableAlert
): EditableAlert => {
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
    if (subscriptionGroupsToAlertIdsMap) {
        uiAlert.subscriptionGroups =
            subscriptionGroupsToAlertIdsMap.get(alert.id) || [];
    }

    if (alert.template && alert.template.nodes) {
        alert.template.nodes.forEach((alertNode) => {
            if (alertNode.type === AlertNodeType.ANOMALY_DETECTOR.toString()) {
                if ((alertNode as AlertAnomalyDetectorNode).params) {
                    uiAlert.detectionTypes.push(
                        (alertNode as AlertAnomalyDetectorNode).params.type
                    );
                }
            }
        });
    }

    if (alert.templateProperties && alert.template) {
        renderMetadataFromAlert(alert, uiAlert);
    }

    return uiAlert;
};

const renderMetadataFromAlert = (alert: Alert, uiAlert: UiAlert): void => {
    if (!alert.template || !alert.template.metadata) {
        return;
    }

    const metadataValueKeyExtractor = /\$\{(.*)\}/;

    // This is done so we avoid a weird typescript error
    // `alert.template.metadata` may be null even though its checked
    const metadata = alert.template.metadata;

    Object.keys(metadata).forEach((metadataKey) => {
        const metadataObjectForKey = metadata[metadataKey];

        if (metadataObjectForKey.name) {
            const templatePropKeySearch = metadataObjectForKey.name.match(
                metadataValueKeyExtractor
            );

            if (templatePropKeySearch && templatePropKeySearch.length > 1) {
                const value =
                    alert.templateProperties[templatePropKeySearch[1]];
                if (value) {
                    uiAlert.renderedMetadata.push({
                        key: metadataKey,
                        value: value,
                    });
                }
            } else {
                // If regex doesn't match then assume no value is being
                // taken from template properties
                uiAlert.renderedMetadata.push({
                    key: metadataKey,
                    value: metadataObjectForKey.name,
                });
            }
        }
    });

    uiAlert.renderedMetadata = sortBy(uiAlert.renderedMetadata, "key");
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

export const DEFAULT_FEEDBACK = {
    type: AnomalyFeedbackType.NO_FEEDBACK,
    comment: "",
};
