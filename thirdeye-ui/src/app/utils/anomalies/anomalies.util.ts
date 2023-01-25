/*
 * Copyright 2022 StarTree Inc
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
import { AxiosError } from "axios";
import bounds from "binary-search-bounds";
import i18n from "i18next";
import { cloneDeep, every, isEmpty, isNil, isNumber } from "lodash";
import { Dispatch, SetStateAction } from "react";
import { NotificationTypeV1 } from "../../platform/components";
import {
    formatDateAndTimeV1,
    formatDurationV1,
    formatLargeNumberV1,
    formatPercentageV1,
} from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { createAlert } from "../../rest/alerts/alerts.rest";
import {
    AlertTemplate,
    AlertTemplate as AlertTemplateType,
} from "../../rest/dto/alert-template.interfaces";
import {
    Alert,
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import {
    Anomaly,
    AnomalyFeedbackType,
} from "../../rest/dto/anomaly.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { updateSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import { getErrorMessages } from "../rest/rest.util";
import { deepSearchStringProperty } from "../search/search.util";

export const EMPTY_STRING_DISPLAY = "<EMPTY_VALUE>";

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly || isNil(anomaly.id)) {
        return i18n.t("label.anomaly");
    }

    return `${i18n.t("label.anomaly")} ${i18n.t("label.entity-id", {
        id: anomaly.id,
    })}`;
};

export const createEmptyUiAnomaly = (): UiAnomaly => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        alertId: -1,
        alertName: noDataMarker,
        current: noDataMarker,
        metricId: -1,
        metricName: noDataMarker,
        currentVal: -1,
        predicted: noDataMarker,
        predictedVal: -1,
        deviation: noDataMarker,
        deviationVal: -1,
        negativeDeviation: false,
        duration: noDataMarker,
        durationVal: 0,
        startTime: noDataMarker,
        endTime: noDataMarker,
        endTimeVal: -1,
        startTimeVal: -1,
        datasetName: noDataMarker,
        hasFeedback: false,
        isIgnored: false,
    };
};

export const createAlertEvaluation = (
    alertId: number,
    startTime: number,
    endTime: number
): AlertEvaluation => {
    return {
        alert: {
            id: alertId,
        },
        start: startTime,
        end: endTime,
    } as AlertEvaluation;
};

export const getUiAnomaly = (anomaly: Anomaly): UiAnomaly => {
    const uiAnomaly = createEmptyUiAnomaly();

    if (!anomaly) {
        return uiAnomaly;
    }

    const noDataMarker = i18n.t("label.no-data-marker");

    // Basic properties
    uiAnomaly.id = anomaly.id;
    uiAnomaly.name = getAnomalyName(anomaly);

    // Alert properties
    if (anomaly.alert) {
        uiAnomaly.alertId = anomaly.alert.id;
        uiAnomaly.alertName = anomaly.alert.name || noDataMarker;
    }

    // Current and predicted values
    if (isNumber(anomaly.avgCurrentVal)) {
        uiAnomaly.current = formatLargeNumberV1(anomaly.avgCurrentVal);
        uiAnomaly.currentVal = anomaly.avgCurrentVal;
    }
    if (isNumber(anomaly.avgBaselineVal)) {
        uiAnomaly.predicted = formatLargeNumberV1(anomaly.avgBaselineVal);
        uiAnomaly.predictedVal = anomaly.avgBaselineVal;
    }

    // Calculate deviation if both current and average values are available
    if (isNumber(anomaly.avgCurrentVal) && isNumber(anomaly.avgBaselineVal)) {
        const deviation =
            (anomaly.avgCurrentVal - anomaly.avgBaselineVal) /
            Math.abs(anomaly.avgBaselineVal);

        if (isNaN(deviation)) {
            uiAnomaly.deviation = i18n.t("label.no-data-marker");
        } else if (deviation === Infinity) {
            uiAnomaly.deviation = "Large(Infinite)";
        } else {
            uiAnomaly.deviation = formatPercentageV1(
                deviation,
                2,
                true,
                deviation > 10 ** 5
            );
        }

        uiAnomaly.deviationVal = deviation;
        uiAnomaly.negativeDeviation = deviation < 0;
    }

    // Start and end time
    if (anomaly.startTime) {
        uiAnomaly.startTime = formatDateAndTimeV1(anomaly.startTime);
        uiAnomaly.startTimeVal = anomaly.startTime;
    }
    if (anomaly.endTime) {
        uiAnomaly.endTime = formatDateAndTimeV1(anomaly.endTime);
        uiAnomaly.endTimeVal = anomaly.endTime;
    }

    // Duration
    if (anomaly.startTime && anomaly.endTime) {
        uiAnomaly.duration = formatDurationV1(
            anomaly.startTime,
            anomaly.endTime
        );
        uiAnomaly.durationVal = anomaly.endTime - anomaly.startTime;
    }

    // Metric
    if (anomaly.metric) {
        uiAnomaly.metricId = anomaly.metric.id;
        uiAnomaly.metricName = anomaly.metric.name;
    }

    // metadata
    if (anomaly.metadata) {
        if (anomaly.metadata.dataset) {
            uiAnomaly.datasetName = anomaly.metadata.dataset.name;
        }
    }

    // has feedback
    if (anomaly.feedback) {
        // In the get all anomalies response, an id may exist
        // In the get one anomaly response, type may exist if there is feedback
        if (
            anomaly.feedback.id ||
            anomaly.feedback.type !== AnomalyFeedbackType.NO_FEEDBACK
        ) {
            uiAnomaly.hasFeedback = true;
        }
    }

    if (anomaly.anomalyLabels) {
        uiAnomaly.isIgnored = isAnomalyIgnored(anomaly);
    }

    if (anomaly.enumerationItem?.id) {
        uiAnomaly.enumerationId = anomaly.enumerationItem?.id;
    }

    return uiAnomaly;
};

export const getUiAnomalies = (anomalies: Anomaly[]): UiAnomaly[] => {
    if (isEmpty(anomalies)) {
        return [];
    }

    const uiAnomalies = [];
    for (const anomaly of anomalies) {
        uiAnomalies.push(getUiAnomaly(anomaly));
    }

    return uiAnomalies;
};

export const filterAnomalies = (
    uiAnomalies: UiAnomaly[],
    searchWords: string[]
): UiAnomaly[] => {
    if (isEmpty(uiAnomalies)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiAnomalies;
    }

    const filteredUiAnomalies = [];
    for (const uiAnomaly of uiAnomalies) {
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiAnomaly,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiAnomalies.push(uiAnomaly);

                break;
            }
        }
    }

    return filteredUiAnomalies;
};

export const filterAnomaliesByTime = (
    anomalies: Anomaly[],
    startTime: number,
    endTime: number
): Anomaly[] => {
    if (!anomalies) {
        return [];
    }

    if (isNil(startTime) || isNil(endTime)) {
        return anomalies;
    }

    // Anomalies may not be sorted, sort by anomaly start time
    let sortedAnomalies = cloneDeep(anomalies).sort(
        anomaliesStartTimeComparator
    );

    // Search last anomaly with start time less than or equal to filter end time
    const indexByStartTime = bounds.le(
        sortedAnomalies,
        { startTime: endTime } as Anomaly,
        anomaliesStartTimeComparator
    );
    if (indexByStartTime === -1) {
        // Not found
        return [];
    }

    // Remove anomalies with start time beyond filter end time
    sortedAnomalies = sortedAnomalies.slice(0, indexByStartTime + 1);

    // Sort by anomaly end time
    sortedAnomalies.sort(anomaliesEndTimeComparator);

    // Search first anomaly with end time greater than or equal to filter start time
    const indexByEndTime = bounds.ge(
        sortedAnomalies,
        { endTime: startTime } as Anomaly,
        anomaliesEndTimeComparator
    );

    // Remove anomalies with end time before filter start time
    sortedAnomalies = sortedAnomalies.slice(
        indexByEndTime,
        sortedAnomalies.length
    );

    return sortedAnomalies;
};

export const getAnomaliesAtTime = (
    anomalies: Anomaly[],
    time: number
): Anomaly[] => {
    if (!anomalies) {
        return [];
    }

    if (isNil(time)) {
        return anomalies;
    }

    // Anomalies may not be sorted, sort by anomaly start time
    let sortedAnomalies = cloneDeep(anomalies).sort(
        anomaliesStartTimeComparator
    );

    // Search last anomaly with start time less than or equal to filter time
    const indexByStartTime = bounds.le(
        sortedAnomalies,
        { startTime: time } as Anomaly,
        anomaliesStartTimeComparator
    );
    if (indexByStartTime === -1) {
        // Not found
        return [];
    }

    // Remove anomalies with start time beyond filter time
    sortedAnomalies = sortedAnomalies.slice(0, indexByStartTime + 1);

    // Sort by anomaly end time
    sortedAnomalies.sort(anomaliesEndTimeComparator);

    // Search first anomaly with end time greater than or equal to filter time
    const indexByEndTime = bounds.ge(
        sortedAnomalies,
        { endTime: time } as Anomaly,
        anomaliesEndTimeComparator
    );

    // Remove anomalies with end time before filter time
    sortedAnomalies = sortedAnomalies.slice(
        indexByEndTime,
        sortedAnomalies.length
    );

    return sortedAnomalies;
};

const anomaliesStartTimeComparator = (
    anomaly1: Anomaly,
    anomaly2: Anomaly
): number => {
    return anomaly1.startTime - anomaly2.startTime;
};

const anomaliesEndTimeComparator = (
    anomaly1: Anomaly,
    anomaly2: Anomaly
): number => {
    return anomaly1.endTime - anomaly2.endTime;
};

export const getShortText = (
    text: string,
    nodeWidth: number,
    nodeHeight: number
): string => {
    const estimatedTextLength = text.length * 10;
    const textToShow: string =
        estimatedTextLength > nodeWidth
            ? text.substring(0, nodeWidth / 10) + ".."
            : text;
    if (nodeWidth <= 30 && nodeHeight <= 20) {
        return "";
    }

    return textToShow;
};

export const ANOMALY_OPTIONS_TO_DESCRIPTIONS = {
    [AnomalyFeedbackType.ANOMALY.valueOf()]: "Yes, this is a valid anomaly",
    [AnomalyFeedbackType.ANOMALY_EXPECTED.valueOf()]:
        "Yes, this anomaly is expected",
    [AnomalyFeedbackType.ANOMALY_NEW_TREND.valueOf()]:
        "Yes, however, this may be a new trend",
};

export const ALL_OPTIONS_TO_DESCRIPTIONS = {
    ...ANOMALY_OPTIONS_TO_DESCRIPTIONS,
    [AnomalyFeedbackType.NOT_ANOMALY.valueOf()]: "No, this is not an anomaly",
};

export const ALL_OPTIONS_WITH_NO_FEEDBACK = {
    ...ALL_OPTIONS_TO_DESCRIPTIONS,
    [AnomalyFeedbackType.NO_FEEDBACK.valueOf()]: "Select Feedback",
};

export const handleAlertPropertyChangeGenerator = (
    setAlert: Dispatch<SetStateAction<EditableAlert>>,
    alertTemplateOptions: AlertTemplateType[],
    setSelectedAlertTemplate: Dispatch<SetStateAction<AlertTemplate | null>>,
    translation: (label: string) => string
) => {
    return (
        contentsToReplace: Partial<EditableAlert>,
        isTotalReplace = false
    ): void => {
        if (isTotalReplace) {
            setAlert(contentsToReplace as EditableAlert);

            if (!contentsToReplace.template) {
                setSelectedAlertTemplate(null);
            }
        } else {
            setAlert((currentAlert) => {
                return {
                    ...currentAlert,
                    ...contentsToReplace,
                } as EditableAlert;
            });
        }

        if (
            alertTemplateOptions &&
            contentsToReplace.template &&
            contentsToReplace.template.name
        ) {
            // If the alert template refers to an alert template
            const selectedAlertTemplateName = contentsToReplace.template.name;
            const match = alertTemplateOptions.find(
                (candidate) => candidate.name === selectedAlertTemplateName
            );
            setSelectedAlertTemplate(match === undefined ? null : match);
        } else if (contentsToReplace.template) {
            if (Object.keys(contentsToReplace.template).length === 0) {
                // Set empty if user removed contents
                setSelectedAlertTemplate(null);
            } else if (contentsToReplace.template.name === undefined) {
                // If user just throws template into the configuration, treat is as custom
                setSelectedAlertTemplate({
                    name: translation("message.custom-alert-template-used"),
                    ...(contentsToReplace.template as Partial<AlertTemplateType>),
                } as AlertTemplateType);
            }
        }
    };
};

export const handleCreateAlertClickGenerator = (
    notify: (type: NotificationTypeV1, msg: string) => void,
    translation: (label: string, options?: { [key: string]: string }) => string,
    onSuccess: (alert: Alert) => void
) => {
    return (
        alert: EditableAlert,
        subscriptionGroups: SubscriptionGroup[],
        onAlertCreateStatusUpdate?: (status: ActionStatus) => void
    ): void => {
        onAlertCreateStatusUpdate &&
            onAlertCreateStatusUpdate(ActionStatus.Working);

        createAlert(alert)
            .then((alert) => {
                if (isEmpty(subscriptionGroups)) {
                    // Call the onSuccess callback
                    onSuccess(alert);
                    onAlertCreateStatusUpdate &&
                        onAlertCreateStatusUpdate(ActionStatus.Done);

                    return;
                }

                // Update subscription groups with new alert
                for (const subscriptionGroup of subscriptionGroups) {
                    if (subscriptionGroup.alerts) {
                        // Add to existing list
                        subscriptionGroup.alerts.push(alert);
                    } else {
                        // Create and add to list
                        subscriptionGroup.alerts = [alert];
                    }
                }

                updateSubscriptionGroups(subscriptionGroups)
                    .then((): void => {
                        notify(
                            NotificationTypeV1.Success,
                            translation("message.update-success", {
                                entity: translation(
                                    "label.subscription-groups"
                                ),
                            })
                        );
                    })
                    .finally((): void => {
                        // Call the onSuccess callback
                        onSuccess(alert);
                        onAlertCreateStatusUpdate &&
                            onAlertCreateStatusUpdate(ActionStatus.Done);
                    });
            })
            .catch((error: AxiosError) => {
                onAlertCreateStatusUpdate &&
                    onAlertCreateStatusUpdate(ActionStatus.Error);
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          translation("message.create-error", {
                              entity: translation("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };
};

export const isAnomalyIgnored = (anomaly: Anomaly): boolean =>
    !!(
        anomaly?.anomalyLabels &&
        anomaly?.anomalyLabels.some((label) => label.ignore)
    );

export const filterOutIgnoredAnomalies = (anomalies: Anomaly[]): Anomaly[] => {
    // Filter out anomalies that should be ignored if it has a label with ignore in it
    return anomalies.filter((anomaly: Anomaly) => {
        if (!anomaly.anomalyLabels) {
            return true;
        }

        return every(
            anomaly.anomalyLabels.map((label) => {
                return label.ignore === false || label.ignore === undefined;
            })
        );
    });
};

// Generic function to filter anomalies by filter functions
export const filterAnomaliesByFunctions = (
    anomalies: Anomaly[],
    filters: ((a: Anomaly) => boolean)[] = []
): Anomaly[] => {
    if (filters.length === 0) {
        return anomalies;
    }

    return anomalies.filter((anomaly) => {
        return filters.every((f) => f(anomaly));
    });
};
