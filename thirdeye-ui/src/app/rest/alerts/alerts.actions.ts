import { useHTTPAction } from "../create-rest-action";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    GetAlert,
    GetEvaluation,
    UseGetEvaluationParams,
} from "./alerts.interfaces";
import { getAlert as getAlertREST, getAlertEvaluation } from "./alerts.rest";

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
