import i18n from "i18next";
import { isEmpty } from "lodash";
import { AnomalyCardData } from "../../components/anomaly-card/anomaly-card.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    formatDuration,
    formatLongDateAndTime,
} from "../date-time/date-time-util";
import { formatLargeNumber, formatPercentage } from "../number/number-util";

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly) {
        return i18n.t("label.no-data-available-marker");
    }

    return `${i18n.t("label.anomaly")} ${i18n.t("label.anomaly-id", {
        id: anomaly.id,
    })}`;
};

export const getAnomalyCardData = (anomaly: Anomaly): AnomalyCardData => {
    if (isEmpty(anomaly)) {
        return {} as AnomalyCardData;
    }

    return getAnomalyCardDatas([anomaly])[0];
};

export const getAnomalyCardDatas = (
    anomalies: Anomaly[]
): AnomalyCardData[] => {
    const anomalyCardDatas: AnomalyCardData[] = [];

    if (isEmpty(anomalies)) {
        return anomalyCardDatas;
    }

    for (const anomaly of anomalies) {
        const anomalyCardData = {} as AnomalyCardData;

        // Basic properties
        anomalyCardData.id = anomaly.id;
        anomalyCardData.name = getAnomalyName(anomaly);
        anomalyCardData.alertName = anomaly.alert?.name
            ? anomaly.alert.name
            : i18n.t("label.no-data-available-marker");
        anomalyCardData.alertId = anomaly.alert?.id;

        // Current and predicted values, if available
        anomalyCardData.current = anomaly.avgCurrentVal
            ? formatLargeNumber(anomaly.avgCurrentVal)
            : i18n.t("label.no-data-available-marker");
        anomalyCardData.predicted = anomaly.avgBaselineVal
            ? formatLargeNumber(anomaly.avgBaselineVal)
            : i18n.t("label.no-data-available-marker");

        // Calculate deviation if both average and current values are available
        anomalyCardData.deviation = i18n.t("label.no-data-available-marker");
        if (anomaly.avgCurrentVal && anomaly.avgBaselineVal) {
            const deviation =
                (anomaly.avgCurrentVal - anomaly.avgBaselineVal) /
                anomaly.avgBaselineVal;
            anomalyCardData.deviation = formatPercentage(deviation);
            anomalyCardData.negativeDeviation = deviation < 0;
        }

        // Duration, start and end time
        anomalyCardData.duration = i18n.t("label.no-data-available-marker");
        anomalyCardData.startTime = i18n.t("label.no-data-available-marker");
        anomalyCardData.endTime = i18n.t("label.no-data-available-marker");
        if (anomaly.startTime && anomaly.endTime) {
            anomalyCardData.duration = formatDuration(
                anomaly.startTime,
                anomaly.endTime
            );
            anomalyCardData.startTime = formatLongDateAndTime(
                anomaly.startTime
            );
            anomalyCardData.endTime = formatLongDateAndTime(anomaly.endTime);
        }

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
