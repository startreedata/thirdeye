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
import { useHTTPAction } from "../create-rest-action";
import {
    Alert,
    AlertEvaluation,
    AlertInsight,
    AlertStats,
    EditableAlert,
} from "../dto/alert.interfaces";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";
import {
    GetAlert,
    GetAlertEvaluationPayload,
    GetAlertInsight,
    GetAlerts,
    GetAlertStats,
    GetAlertStatsParams,
    GetEvaluation,
    ResetAlert,
} from "./alerts.interfaces";
import {
    getAlert as getAlertREST,
    getAlertEvaluation,
    getAlertInsight as getAlertInsightREST,
    getAlertStats as getAlertStatsREST,
    getAllAlerts,
    resetAlert as resetAlertREST,
} from "./alerts.rest";

export const useGetEvaluation = (): GetEvaluation => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<AlertEvaluation>(getAlertEvaluation);

    const getEvaluation = (
        evaluationParams: GetAlertEvaluationPayload,
        filters?: string[],
        enumerationItem?: { id: number } | EnumerationItem
    ): Promise<AlertEvaluation | undefined> => {
        return makeRequest(evaluationParams, filters, enumerationItem);
    };

    return {
        evaluation: data,
        getEvaluation,
        status,
        errorMessages,
        resetData,
    };
};

export const useGetAlert = (): GetAlert => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Alert>(getAlertREST);

    const getAlert = (alertId: number): Promise<Alert | undefined> => {
        return makeRequest(alertId);
    };

    return { alert: data, getAlert, status, errorMessages, resetData };
};

export const useGetAlerts = (): GetAlerts => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Alert[]>(getAllAlerts);

    const getAlerts = (): Promise<Alert[] | undefined> => {
        return makeRequest();
    };

    return { alerts: data, getAlerts, status, errorMessages, resetData };
};

export const useGetAlertInsight = (): GetAlertInsight => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<AlertInsight>(getAlertInsightREST);

    const getAlertInsight = (params: {
        alertId?: number;
        alert?: EditableAlert;
    }): Promise<AlertInsight | undefined> => {
        return makeRequest(params);
    };

    return {
        alertInsight: data,
        getAlertInsight,
        status,
        errorMessages,
        resetData,
    };
};

export const useResetAlert = (): ResetAlert => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<Alert>(resetAlertREST);

    const resetAlert = (alertId: number): Promise<Alert | undefined> => {
        return makeRequest(alertId);
    };

    return { alert: data, resetAlert, status, errorMessages, resetData };
};

export const useGetAlertStats = (): GetAlertStats => {
    const { data, makeRequest, status, errorMessages, resetData } =
        useHTTPAction<AlertStats>(getAlertStatsREST);

    const getAlertStats = (
        params: GetAlertStatsParams
    ): Promise<AlertStats | undefined> => {
        return makeRequest(params);
    };

    return {
        alertStats: data,
        getAlertStats,
        status,
        errorMessages,
        resetData,
    };
};
