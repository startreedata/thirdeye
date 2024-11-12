/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import i18n from "i18next";
import { cloneDeep, isEmpty, kebabCase, omit } from "lodash";
import { PropertyRenderConfig } from "../../components/alert-wizard-v2/alert-template/alert-template-properties-builder/alert-template-properties-builder.interfaces";
import { GetAlertEvaluationPayload } from "../../rest/alerts/alerts.interfaces";
import {
    Alert,
    AlertAnomalyDetectorNode,
    AlertEvaluation,
    AlertNodeType,
    AlertStats,
    EditableAlert,
    EvaluatedTemplateMetadata,
} from "../../rest/dto/alert.interfaces";
import { AnomalyFeedbackType } from "../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../rest/dto/detection.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    UiAlert,
    UiAlertDatasetAndMetric,
    UiAlertSubscriptionGroup,
} from "../../rest/dto/ui-alert.interfaces";
import { deepSearchStringProperty } from "../search/search.util";
import { getSubscriptionGroupAlertsList } from "../subscription-groups/subscription-groups.util";
import { DAY_IN_MILLISECONDS, iso8601ToMilliseconds } from "../time/time.util";

export const DEFAULT_FEEDBACK = {
    type: AnomalyFeedbackType.NO_FEEDBACK,
    comment: "",
};

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
    alert: Alert | EditableAlert | Pick<Alert, "id">,
    startTime: number,
    endTime: number,
    evaluationContext?: { listEnumerationItemsOnly: boolean }
): GetAlertEvaluationPayload => {
    const basePayload: GetAlertEvaluationPayload = {
        alert: alert,
        start: startTime,
        end: endTime,
    };
    if (evaluationContext) {
        basePayload["evaluationContext"] = evaluationContext;
    }

    return basePayload;
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
        generateAlertIdToSubscriptionGroupMap(subscriptionGroups);

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
        generateAlertIdToSubscriptionGroupMap(subscriptionGroups);

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
    const newAlert = omit(alert, ["id", "created", "updated"]);

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

    return uiAlert;
};

const generateAlertIdToSubscriptionGroupMap = (
    subscriptionGroups: SubscriptionGroup[]
): Map<number, UiAlertSubscriptionGroup[]> => {
    const alertIdToSubscriptionGroup = new Map();

    if (isEmpty(subscriptionGroups)) {
        return alertIdToSubscriptionGroup;
    }

    for (const subscriptionGroup of subscriptionGroups) {
        if (
            isEmpty(subscriptionGroup.alerts) &&
            isEmpty(subscriptionGroup.alertAssociations)
        ) {
            continue;
        }

        const uiAlertSubscriptionGroup = createEmptyUiAlertSubscriptionGroup();
        uiAlertSubscriptionGroup.id = subscriptionGroup.id;
        uiAlertSubscriptionGroup.name =
            subscriptionGroup.name || i18n.t("label.no-data-marker");

        const alerts = getSubscriptionGroupAlertsList(subscriptionGroup);
        for (const alert of alerts) {
            const subscriptionGroupsForAlert =
                alertIdToSubscriptionGroup.get(alert.id) || [];

            subscriptionGroupsForAlert.push(uiAlertSubscriptionGroup);
            alertIdToSubscriptionGroup.set(
                alert.id,
                subscriptionGroupsForAlert
            );
        }
    }

    // Ensure subscription group is unique i nlist
    for (const [alertId, subscriptionGroups] of alertIdToSubscriptionGroup) {
        const idToSubscriptionGroup: {
            [index: number]: UiAlertSubscriptionGroup;
        } = {};

        subscriptionGroups.forEach(
            (subscriptionGroup: UiAlertSubscriptionGroup) => {
                idToSubscriptionGroup[subscriptionGroup.id] = subscriptionGroup;
            }
        );

        alertIdToSubscriptionGroup.set(
            alertId,
            Object.values(idToSubscriptionGroup)
        );
    }

    return alertIdToSubscriptionGroup;
};

export const extractDetectionEvaluation = (
    evaluationDataPayload: AlertEvaluation
): DetectionEvaluation[] => {
    return Object.keys(evaluationDataPayload.detectionEvaluations).map(
        (k) => evaluationDataPayload.detectionEvaluations[k]
    );
};

export const generateGenericNameForAlert = (
    metricName: string,
    aggregationFunction?: string,
    algorithmName?: string,
    isMultidimension?: boolean
): string => {
    let nameSoFar = metricName;

    if (aggregationFunction) {
        nameSoFar += `_${aggregationFunction}`;
    }

    if (algorithmName) {
        nameSoFar += `_${kebabCase(algorithmName)}`;
    }

    if (isMultidimension) {
        nameSoFar += `_dx`;
    }

    return nameSoFar;
};

type AlertAccuracyColor = "success" | "warning" | "error";

/**
 *
 * @param alertStat - instance of AlertStats
 * @returns `{accuracy, colorScheme, noAnomalyData}`
 * @param accuracy - number between 0 to 1
 * @param colorScheme - "success" | "warning" | "error",
 */
export const getAlertAccuracyData = (
    alertStat: AlertStats
): {
    accuracy: number;
    colorScheme: AlertAccuracyColor;
    noAnomalyData: boolean;
} => {
    // Default values
    let colorScheme: AlertAccuracyColor = "success";
    let accuracy = 1; // Accuracy fraction, between 0 to 1
    let noAnomalyData = false; // Whether there is any anomaly data at all

    if (alertStat.totalCount === 0) {
        noAnomalyData = true;
    }

    // There's no point of calculating the accuracy if there is no feedback reported.
    // In which case, just return the default value of accuracy=100%
    if (alertStat.totalCount && alertStat.totalCount > 0) {
        // Returns a decimal value with four digits of precision
        accuracy = Number(
            (
                1 -
                alertStat.feedbackStats.NOT_ANOMALY / alertStat.totalCount
            ).toFixed(4)
        );

        if (accuracy < 0.4) {
            colorScheme = "error";
        } else if (accuracy < 0.7) {
            colorScheme = "warning";
        }
    }

    return { accuracy, colorScheme, noAnomalyData };
};

export const determineTimezoneFromAlertInEvaluation = (
    alert: { metadata: EvaluatedTemplateMetadata } | undefined | null
): string | undefined => {
    if (alert === undefined || alert === null) {
        return undefined;
    }

    if (alert.metadata?.timezone) {
        return alert.metadata.timezone as string;
    }

    return "UTC";
};

export const shouldHideTimeInDatetimeFormat = (
    alert: { metadata: EvaluatedTemplateMetadata } | undefined | null
): boolean => {
    if (alert === undefined || alert === null) {
        return false;
    }

    if (alert.metadata?.granularity) {
        return (
            iso8601ToMilliseconds(alert.metadata?.granularity as string) >=
            DAY_IN_MILLISECONDS
        );
    }

    return false;
};

export const extendablePropertyStepNameMap: Record<string, string> = {
    DATA: "Data",
    PREPROCESS: "Preprocessing",
    DETECTION: "Detection",
    DIMENSION_EXPLORATION: "Dimension Exploration",
    FILTER: "Filtering",
    POSTPROCESS: "Postprocessing",
    RCA: "Root Cause Analysis",
    OTHER: "Other",
};
export const extendablePropertyStepNameMapV2: Record<string, string> = {
    DATA: "Sample data",
    PREPROCESS: "Preprocessing",
    DETECTION: "Detection",
    DIMENSION_EXPLORATION: "Dimension Exploration",
    FILTER: "Filtering",
    POSTPROCESS: "Postprocessing",
    RCA: "Root Cause Analysis",
    OTHER: "Other",
};

export enum ALERT_PROPERTIES_DEFAULT_STEP {
    STEP = "OTHER",
    SUBSTEP = "DIRECT",
}

export const ALERT_PROPERTIES_DEFAULT_ORDER = [
    "DATA",
    "DIMENSION_EXPLORATION",
    "PREPROCESS",
    "DETECTION",
    "FILTER",
    "POSTPROCESS",
    "RCA",
    "OTHER",
];

export const groupPropertyByStepAndSubStep = (
    properties: PropertyRenderConfig[]
): Record<string, Record<string, PropertyRenderConfig[]>> => {
    const groupedProperties: Record<
        string,
        Record<string, PropertyRenderConfig[]>
    > = {};

    for (const property of properties) {
        const step =
            property.metadata.step || ALERT_PROPERTIES_DEFAULT_STEP.STEP;
        if (!groupedProperties[step]) {
            groupedProperties[step] = {};
        }
        const subStep =
            property.metadata.subStep || ALERT_PROPERTIES_DEFAULT_STEP.SUBSTEP;
        if (!groupedProperties?.[step]?.[subStep]) {
            groupedProperties[step][subStep] = [];
        }

        groupedProperties[step][subStep].push(property);
    }

    return groupedProperties;
};

export const getGranularityLabelFromValue = (granularity: string): string => {
    switch (granularity) {
        case "P7D":
            return i18n.t("label.weekly");
        case "P1D":
            return i18n.t("label.daily");
        case "PT1H":
            return i18n.t("label.hourly");
        case "PT15M":
            return i18n.t("label.15-minutes");
        case "PT5M":
            return i18n.t("label.5-minutes");
        case "PT1M":
            return i18n.t("label.1-minute");
        default:
            return granularity;
    }
};

export const isValidISO8601 = (duration: string): boolean => {
    // ISO-8601 duration regex pattern
    const iso8601Pattern =
        /^P(?!$)(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(?=\d)(\d+H)?(\d+M)?(\d+S)?)?$/;

    if (!duration) {
        return false;
    }

    try {
        // Test if the string matches the ISO-8601 duration pattern
        return iso8601Pattern.test(duration);
    } catch (error) {
        return false;
    }
};
