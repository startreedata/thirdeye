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
