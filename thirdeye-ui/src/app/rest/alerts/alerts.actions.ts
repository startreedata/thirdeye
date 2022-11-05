import { useHTTPAction } from "../create-rest-action";
import {
    Alert,
    AlertEvaluation,
    AlertInsight,
    EditableAlert,
} from "../dto/alert.interfaces";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";
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
        filters?: string[],
        enumerationItem?: { id: number } | EnumerationItem
    ): Promise<AlertEvaluation | undefined> => {
        return makeRequest(evaluationParams, filters, enumerationItem);
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

    const getAlertInsight = (params: {
        alertId?: number;
        alert?: EditableAlert;
    }): Promise<AlertInsight | undefined> => {
        return makeRequest(params);
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
