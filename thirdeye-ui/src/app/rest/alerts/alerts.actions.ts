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
import { useHTTPAction } from "../create-rest-action";
import { Alert, AlertEvaluation, AlertInsight } from "../dto/alert.interfaces";
import {
    GetAlert,
    GetAlertInsight,
    GetAlerts,
    GetEvaluation,
    ResetAlert,
    UseGetEvaluationParams,
} from "./alerts.interfaces";
import {
    getAlert as getAlertREST,
    getAlertEvaluation,
    getAlertInsight as getAlertInsightREST,
    getAllAlerts,
    resetAlert as resetAlertREST,
} from "./alerts.rest";

export const useGetEvaluation = (): GetEvaluation => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AlertEvaluation>(getAlertEvaluation);

    const getEvaluation = (
        evaluationParams: UseGetEvaluationParams,
        filters?: string[]
    ): Promise<AlertEvaluation | undefined> => {
        return makeRequest(evaluationParams, filters);
    };

    return { evaluation: data, getEvaluation, status, errorMessages };
};

export const useGetAlert = (): GetAlert => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Alert>(getAlertREST);

    const getAlert = (alertId: number): Promise<Alert | undefined> => {
        return makeRequest(alertId);
    };

    return { alert: data, getAlert, status, errorMessages };
};

export const useGetAlerts = (): GetAlerts => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Alert[]>(getAllAlerts);

    const getAlerts = (): Promise<Alert[] | undefined> => {
        return makeRequest();
    };

    return { alerts: data, getAlerts, status, errorMessages };
};

export const useGetAlertInsight = (): GetAlertInsight => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AlertInsight>(getAlertInsightREST);

    const getAlertInsight = (
        alertId: number
    ): Promise<AlertInsight | undefined> => {
        return makeRequest(alertId);
    };

    return { alertInsight: data, getAlertInsight, status, errorMessages };
};

export const useResetAlert = (): ResetAlert => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Alert>(resetAlertREST);

    const resetAlert = (alertId: number): Promise<Alert | undefined> => {
        return makeRequest(alertId);
    };

    return { alert: data, resetAlert, status, errorMessages };
};
