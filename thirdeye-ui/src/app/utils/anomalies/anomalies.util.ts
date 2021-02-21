import bounds from "binary-search-bounds";
import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { formatDateAndTime, formatDuration } from "../date-time/date-time.util";
import { formatLargeNumber, formatPercentage } from "../number/number.util";
import { deepSearchStringProperty } from "../search/search.util";

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly || !anomaly.id) {
        return i18n.t("label.anomaly");
    }

    return `${i18n.t("label.anomaly")} ${i18n.t("label.entity-id", {
        id: anomaly.id,
    })}`;
};

export const createEmptyAnomalyCardData = (): AnomalyCardData => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        alertId: -1,
        alertName: noDataMarker,
        current: noDataMarker,
        predicted: noDataMarker,
        deviation: noDataMarker,
        negativeDeviation: false,
        duration: noDataMarker,
        startTime: noDataMarker,
        endTime: noDataMarker,
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

export const getAnomalyCardData = (anomaly: Anomaly): AnomalyCardData => {
    const anomalyCardData = createEmptyAnomalyCardData();

    if (!anomaly) {
        return anomalyCardData;
    }

    const noDataMarker = i18n.t("label.no-data-marker");

    // Basic properties
    anomalyCardData.id = anomaly.id;
    anomalyCardData.name = getAnomalyName(anomaly);

    // Alert properties
    if (anomaly.alert) {
        anomalyCardData.alertId = anomaly.alert.id;
        anomalyCardData.alertName = anomaly.alert.name || noDataMarker;
    }

    // Current and predicted values
    if (anomaly.avgCurrentVal) {
        anomalyCardData.current = formatLargeNumber(anomaly.avgCurrentVal);
    }
    if (anomaly.avgBaselineVal) {
        anomalyCardData.predicted = formatLargeNumber(anomaly.avgBaselineVal);
    }

    // Calculate deviation if both current and average values are available
    if (anomaly.avgCurrentVal && anomaly.avgBaselineVal) {
        const deviation =
            (anomaly.avgCurrentVal - anomaly.avgBaselineVal) /
            anomaly.avgBaselineVal;
        anomalyCardData.deviation = formatPercentage(deviation);
        anomalyCardData.negativeDeviation = deviation < 0;
    }

    // Start and end time
    if (anomaly.startTime) {
        anomalyCardData.startTime = formatDateAndTime(anomaly.startTime);
    }
    if (anomaly.endTime) {
        anomalyCardData.endTime = formatDateAndTime(anomaly.endTime);
    }

    // Duration
    if (anomaly.startTime && anomaly.endTime) {
        anomalyCardData.duration = formatDuration(
            anomaly.startTime,
            anomaly.endTime
        );
    }

    return anomalyCardData;
};

export const getAnomalyCardDatas = (
    anomalies: Anomaly[]
): AnomalyCardData[] => {
    if (isEmpty(anomalies)) {
        return [];
    }

    const anomalyCardDatas = [];
    for (const anomaly of anomalies) {
        anomalyCardDatas.push(getAnomalyCardData(anomaly));
    }

    return anomalyCardDatas;
};

export const filterAnomalies = (
    anomalyCardDatas: AnomalyCardData[],
    searchWords: string[]
): AnomalyCardData[] => {
    if (isEmpty(anomalyCardDatas)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return anomalyCardDatas;
    }

    const filteredAnomalyCardDatas = [];
    for (const anomaly of anomalyCardDatas) {
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    anomaly,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredAnomalyCardDatas.push(anomaly);

                break;
            }
        }
    }

    return filteredAnomalyCardDatas;
};

export const filterAnomaliesByTime = (
    anomalies: Anomaly[],
    startTime: number,
    endTime: number
): Anomaly[] => {
    if (!anomalies) {
        return [];
    }

    if (!startTime || !endTime) {
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

    if (!time) {
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
