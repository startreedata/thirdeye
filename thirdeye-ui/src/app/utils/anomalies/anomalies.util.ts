/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import bounds from "binary-search-bounds";
import i18n from "i18next";
import { cloneDeep, isEmpty, isNil, isNumber } from "lodash";
import {
    formatDateAndTimeV1,
    formatDurationV1,
    formatLargeNumberV1,
    formatPercentageV1,
} from "../../platform/utils";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import {
    Anomaly,
    AnomalyFeedbackType,
} from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
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
            anomaly.avgBaselineVal;
        uiAnomaly.deviation = isNaN(deviation)
            ? i18n.t("label.no-data-marker")
            : formatPercentageV1(deviation);
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
        if (anomaly.metadata?.dataset) {
            uiAnomaly.datasetName = anomaly.metadata.dataset?.name;
        }
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
