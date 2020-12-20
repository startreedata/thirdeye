import i18n from "i18next";
import { isEmpty } from "lodash";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
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

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly) {
        return i18n.t("label.no-data-available-marker");
    }

    return `${i18n.t("label.anomaly")} ${i18n.t("label.anomaly-id", {
        id: anomaly.id,
    })}`;
};

export const createEmptyAnomalyCardData = (): AnomalyCardData => {
    const noDataAvailableMarker = i18n.t("label.no-data-available-marker");

    return {
        id: -1,
        name: noDataAvailableMarker,
        alertName: noDataAvailableMarker,
        alertId: -1,
        current: noDataAvailableMarker,
        predicted: noDataAvailableMarker,
        deviation: noDataAvailableMarker,
        negativeDeviation: false,
        duration: noDataAvailableMarker,
        startTime: noDataAvailableMarker,
        endTime: noDataAvailableMarker,
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

    // Duration, start and end time
    if (anomaly.startTime && anomaly.endTime) {
        anomalyCardData.duration = formatDuration(
            anomaly.startTime,
            anomaly.endTime
        );
        anomalyCardData.startTime = formatDateAndTime(anomaly.startTime);
        anomalyCardData.endTime = formatDateAndTime(anomaly.endTime);
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
        const anomalyCardData = getAnomalyCardData(anomaly);
        anomalyCardDatas.push(anomalyCardData);
    }

    return anomalyCardDatas;
};

export const filterAnomalies = (
    anomalies: AnomalyCardData[],
    searchWords: string[]
): AnomalyCardData[] => {
    const filteredAnomalies = new Set<AnomalyCardData>();

    if (isEmpty(anomalies)) {
        // No anomalies available, return empty result
        return [...filteredAnomalies];
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original anomalies
        return anomalies;
    }

    for (const anomaly of anomalies) {
        for (const searchWord of searchWords) {
            let anomalyFiltered = false;

            // Try and match anomaly property values to search words
            for (const propertyValue of Object.values(anomaly)) {
                if (!propertyValue) {
                    continue;
                }

                if (
                    typeof propertyValue === "string" &&
                    propertyValue
                        .toLowerCase()
                        .indexOf(searchWord.toLowerCase()) > -1
                ) {
                    filteredAnomalies.add(anomaly);
                    anomalyFiltered = true;

                    break;
                }
            }

            if (anomalyFiltered) {
                // Anomaly already filtered, check next anomaly
                break;
            }
        }
    }

    return [...filteredAnomalies];
};
