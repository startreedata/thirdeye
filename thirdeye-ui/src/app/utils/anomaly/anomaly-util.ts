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
        return "";
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
        const anomalyData = {} as AnomalyCardData;
        anomalyData.id = anomaly.id;
        anomalyData.name = getAnomalyName(anomaly);
        anomalyData.alertName = anomaly.alert.name;
        anomalyData.alertId = anomaly.alert.id;

        const current = anomaly.avgCurrentVal
            ? formatLargeNumber(anomaly.avgCurrentVal)
            : i18n.t("label.no-data-available");
        const predicted = anomaly.avgBaselineVal
            ? formatLargeNumber(anomaly.avgBaselineVal)
            : i18n.t("label.no-data-available");
        anomalyData.currentAndPredicted = i18n.t(
            "label.current-/-predicted-values",
            { current: current, predicted: predicted }
        );

        if (anomaly.avgCurrentVal && anomaly.avgBaselineVal) {
            const deviation =
                (anomaly.avgCurrentVal - anomaly.avgBaselineVal) /
                anomaly.avgBaselineVal;
            anomalyData.deviation = formatPercentage(deviation);
            anomalyData.negativeDeviation = deviation < 0;
        }

        anomalyData.duration = formatDuration(
            anomaly.startTime,
            anomaly.endTime
        );
        anomalyData.startTime = formatLongDateAndTime(anomaly.startTime);
        anomalyData.endTime = formatLongDateAndTime(anomaly.endTime);

        anomalyCardDatas.push(anomalyData);
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
        return Array.from(filteredAnomalies);
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original anomalies
        return anomalies;
    }

    for (const anomaly of anomalies) {
        for (const searchWord of searchWords) {
            // Try and match anomaly property values to search words
            for (const propertyValue of Object.values(anomaly)) {
                if (
                    typeof propertyValue === "string" &&
                    propertyValue
                        .toLowerCase()
                        .indexOf(searchWord.toLowerCase()) > -1
                ) {
                    filteredAnomalies.add(anomaly);
                }
            }
        }
    }

    return Array.from(filteredAnomalies);
};
