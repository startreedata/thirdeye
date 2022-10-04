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
import axios from "axios";
import { extractDetectionEvaluation } from "../../utils/alerts/alerts.util";
import { filterOutIgnoredAnomalies } from "../../utils/anomalies/anomalies.util";
import {
    Alert,
    AlertEvaluation,
    AlertInsight,
    EditableAlert,
} from "../dto/alert.interfaces";
import { GetEvaluationRequestPayload } from "./alerts.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const getAlert = async (id: number): Promise<Alert> => {
    const response = await axios.get(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAlertInsight = async ({
    alertId,
    alert,
}: {
    alertId?: number;
    alert?: EditableAlert;
}): Promise<AlertInsight> => {
    let requestPayload: { alert: EditableAlert | { id: number } } = {
        alert: { id: alertId as number },
    };

    if (alert) {
        requestPayload = { alert };
    }

    const response = await axios.post(
        `${BASE_URL_ALERTS}/insights`,
        requestPayload
    );

    return response.data;
};

export const getAllAlerts = async (): Promise<Alert[]> => {
    const response = await axios.get(BASE_URL_ALERTS);

    return response.data;
};

export const createAlert = async (alert: EditableAlert): Promise<Alert> => {
    const response = await axios.post(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const createAlerts = async (alerts: Alert[]): Promise<Alert[]> => {
    const response = await axios.post(BASE_URL_ALERTS, alerts);

    return response.data;
};

export const updateAlert = async (alert: Alert): Promise<Alert> => {
    const response = await axios.put(BASE_URL_ALERTS, [alert]);

    return response.data[0];
};

export const updateAlerts = async (alerts: Alert[]): Promise<Alert[]> => {
    const response = await axios.put(BASE_URL_ALERTS, alerts);

    return response.data;
};

export const deleteAlert = async (id: number): Promise<Alert> => {
    const response = await axios.delete(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const resetAlert = async (id: number): Promise<Alert> => {
    const response = await axios.post(`/api/alerts/${id}/reset`);

    return response.data;
};

export const getAlertEvaluation = async (
    alertEvaluation: AlertEvaluation,
    filters?: string[] // array of strings in `column=value` format
): Promise<AlertEvaluation> => {
    const payload: GetEvaluationRequestPayload = { ...alertEvaluation };

    if (filters && filters.length > 0) {
        payload["evaluationContext"] = { filters };
    }

    const response = await axios.post(`${BASE_URL_ALERTS}/evaluate`, payload);

    if (response.data.detectionEvaluations) {
        extractDetectionEvaluation(response.data).forEach((detectionEval) => {
            detectionEval.anomalies = filterOutIgnoredAnomalies(
                detectionEval.anomalies
            );
        });
    }

    return response.data;
};
