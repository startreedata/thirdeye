/*
 * Copyright 2023 StarTree Inc
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
import axios from "axios";
import { extractDetectionEvaluation } from "../../utils/alerts/alerts.util";
import { filterOutIgnoredAnomalies } from "../../utils/anomalies/anomalies.util";
import type {
    Alert,
    AlertEvaluation,
    AlertInsight,
    AlertStats,
    EditableAlert,
} from "../dto/alert.interfaces";
import {
    DetectionEvaluation,
    EnumerationItemInEvaluation,
} from "../dto/detection.interfaces";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";
import {
    GetAlertEvaluationPayload,
    GetAlertStatsParams,
    GetEvaluationRequestPayload,
} from "./alerts.interfaces";

const BASE_URL_ALERTS = "/api/alerts";

export const getAlert = async (id: number): Promise<Alert> => {
    const response = await axios.get<Alert>(`${BASE_URL_ALERTS}/${id}`);

    return response.data;
};

export const getAlertStats = async ({
    alertId,
    startTime,
    endTime,
}: GetAlertStatsParams): Promise<AlertStats> => {
    const queryParams = new URLSearchParams([]);

    if (startTime) {
        queryParams.set("startTime", `${startTime}`);
    }

    if (endTime) {
        queryParams.set("endTime", `${endTime}`);
    }

    const response = await axios.get<AlertStats>(
        `${BASE_URL_ALERTS}/${alertId}/stats?${queryParams.toString()}`
    );

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

export const rerunAnomalyDetectionForAlert = async ({
    id,
    start,
    end,
}: {
    id: number;
    start?: number;
    end?: number;
}): Promise<Alert> => {
    const params = new URLSearchParams();

    if (start) {
        params.append("start", start.toString());
    }

    if (end) {
        params.append("end", end.toString());
    }

    const response = await axios.post(`/api/alerts/${id}/run`, params);

    return response.data;
};

export const getAlertEvaluation = async (
    alertEvaluation: GetAlertEvaluationPayload,
    filters?: string[], // array of strings in `column=value` format
    enumerationItem?:
        | { id: number }
        | EnumerationItem
        | EnumerationItemInEvaluation
): Promise<AlertEvaluation> => {
    const payload: GetEvaluationRequestPayload = { ...alertEvaluation };

    if ((filters && filters.length > 0) || enumerationItem) {
        payload.evaluationContext = {};

        if (filters && filters.length > 0) {
            payload.evaluationContext = { filters };
        }

        if (enumerationItem) {
            payload.evaluationContext.enumerationItem = enumerationItem;
        }
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

export const getAlertEvaluationEnumerationItems = async (
    alert: Alert | EditableAlert | Pick<Alert, "id">,
    start: number,
    end: number
): Promise<EnumerationItemInEvaluation[]> => {
    const payload: GetEvaluationRequestPayload = {
        alert,
        start,
        end,
        evaluationContext: {
            listEnumerationItemsOnly: true,
        },
    };

    const response = await axios.post(`${BASE_URL_ALERTS}/evaluate`, payload);

    if (response.data.detectionEvaluations) {
        return Object.values<DetectionEvaluation>(
            response.data.detectionEvaluations
        ).map(
            (d: DetectionEvaluation) =>
                d.enumerationItem as EnumerationItemInEvaluation
        );
    }

    return [];
};

export const getAlertRecommendation = async (
    alert: EditableAlert
): Promise<{ alert: EditableAlert }[]> => {
    const response = await axios.post(`${BASE_URL_ALERTS}/recommend`, {
        alert,
    });

    if (!response.data?.analysisRunInfo?.success) {
        return [];
    }

    return response.data.recommendations;
};
