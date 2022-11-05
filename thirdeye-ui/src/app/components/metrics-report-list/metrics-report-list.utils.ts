import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { AnomaliesByAlert } from "./metrics-report-list.interfaces";

export const categorizeAnomalies = (
    anomalies: Anomaly[]
): AnomaliesByAlert[] => {
    const alertToAlertAndAnomalies: { [key: string]: AnomaliesByAlert } = {};

    anomalies.forEach((anomaly) => {
        const bucketName = anomaly.alert.name;
        const bucket = alertToAlertAndAnomalies[bucketName] || {
            alert: anomaly.alert,
            anomalies: [],
            metric: "",
            dataset: "",
        };

        if (anomaly.metadata) {
            if (anomaly.metadata.metric) {
                bucket.metric = anomaly.metadata.metric.name;
            }
            if (anomaly.metadata.dataset) {
                bucket.dataset = anomaly.metadata.dataset.name;
            }
        }

        bucket.anomalies.push(anomaly);

        alertToAlertAndAnomalies[bucketName] = bucket;
    });

    return Object.values(alertToAlertAndAnomalies);
};
