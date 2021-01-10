import i18n from "i18next";
import { isEmpty } from "lodash";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    formatDateAndTime,
    formatDuration,
} from "../date-time-util/date-time-util";
import {
    formatLargeNumber,
    formatPercentage,
} from "../number-util/number-util";
import { deepSearchStringProperty } from "../search-util/search-util";

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly) {
        return i18n.t("label.no-data-marker");
    }

    return `${i18n.t("label.anomaly")} ${i18n.t("label.anomaly-id", {
        id: anomaly.id,
    })}`;
};

export const createEmptyAnomalyCardData = (): AnomalyCardData => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        alertName: noDataMarker,
        alertId: -1,
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

    // Basic properties
    anomalyCardData.id = anomaly.id;
    anomalyCardData.name = getAnomalyName(anomaly);

    // Alert properties
    if (anomaly.alert) {
        anomalyCardData.alertName = anomaly.alert.name;
        anomalyCardData.alertId = anomaly.alert.id;
    }

    // Current and predicted values
    if (anomaly.avgCurrentVal) {
        anomalyCardData.current = formatLargeNumber(anomaly.avgCurrentVal);
    }
    if (anomaly.avgBaselineVal) {
        anomalyCardData.predicted = formatLargeNumber(anomaly.avgBaselineVal);
    }

    // Calculate deviation if both average and current values are available
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
    const anomalyCardDatas: AnomalyCardData[] = [];

    if (isEmpty(anomalies)) {
        return anomalyCardDatas;
    }

    for (const anomaly of anomalies) {
        anomalyCardDatas.push(getAnomalyCardData(anomaly));
    }

    return anomalyCardDatas;
};

export const filterAnomalies = (
    anomalyCardDatas: AnomalyCardData[],
    searchWords: string[]
): AnomalyCardData[] => {
    const filteredAnomalyCardDatas: AnomalyCardData[] = [];

    if (isEmpty(anomalyCardDatas)) {
        // No anomalies available, return empty result
        return filteredAnomalyCardDatas;
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original anomalies
        return anomalyCardDatas;
    }

    for (const anomaly of anomalyCardDatas) {
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(anomaly, (value: string): boolean => {
                    // Check if string property value contains current search word
                    return (
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                    );
                })
            ) {
                filteredAnomalyCardDatas.push(anomaly);

                break;
            }
        }
    }

    return filteredAnomalyCardDatas;
};
