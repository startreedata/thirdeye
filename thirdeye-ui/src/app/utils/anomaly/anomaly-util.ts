import i18n from "i18next";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    formatDuration,
    formatLongDateAndTime,
} from "../date-time/date-time-util";

export const getAnomalyName = (anomaly: Anomaly): string => {
    if (!anomaly) {
        return "";
    }

    return `${i18n.t("label.anomaly")} #${anomaly.id}`;
};

export const filterAnomalies = (
    anomalies: Anomaly[],
    searchWords: string[]
): Anomaly[] => {
    const filteredAnomalies = new Set<Anomaly>();

    if (!anomalies || anomalies.length === 0) {
        // No anomalies available, return empty result
        return Array.from(filteredAnomalies);
    }

    if (!searchWords || searchWords.length === 0) {
        // No search words available, return original anomalies
        return anomalies;
    }

    for (
        let anomaliesIndex = 0;
        anomaliesIndex < anomalies.length;
        anomaliesIndex++
    ) {
        for (
            let searchWordIndex = 0;
            searchWordIndex < searchWords.length;
            searchWordIndex++
        ) {
            // Try and match relevant anomaly property values to search words
            if (
                // Anomaly name
                getAnomalyName(anomalies[anomaliesIndex])
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly duration
                formatDuration(
                    anomalies[anomaliesIndex].startTime,
                    anomalies[anomaliesIndex].endTime
                )
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly start time
                formatLongDateAndTime(anomalies[anomaliesIndex].startTime)
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly end time
                formatLongDateAndTime(anomalies[anomaliesIndex].endTime)
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly alert
                anomalies[anomaliesIndex].alert.name
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly current value
                anomalies[anomaliesIndex].avgCurrentVal
                    .toString()
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1 ||
                // Anomaly current value
                anomalies[anomaliesIndex].avgBaselineVal
                    .toString()
                    .toLowerCase()
                    .indexOf(searchWords[searchWordIndex].toLowerCase()) > -1
            ) {
                filteredAnomalies.add(anomalies[anomaliesIndex]);
            }
        }
    }

    return Array.from(filteredAnomalies);
};
