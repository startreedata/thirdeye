import { useHTTPAction } from "../create-rest-action";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    GetAlert,
    GetEvaluation,
    UseGetEvaluationParams,
} from "./alerts.interfaces";
import { getAlert as getAlertREST, getAlertEvaluation } from "./alerts.rest";

export const useGetEvaluation = (): GetEvaluation => {
    const {
        data,
        makeRequest,
        status,
        errorMessage,
    } = useHTTPAction<AlertEvaluation>(getAlertEvaluation);

    const getEvaluation = (
        evaluationParams: UseGetEvaluationParams
    ): Promise<AlertEvaluation | undefined> => {
        return makeRequest(evaluationParams);
    };

    return { evaluation: data, getEvaluation, status, errorMessage };
};

export const useGetAlert = (): GetAlert => {
    const { data, makeRequest, status, errorMessage } = useHTTPAction<Alert>(
        getAlertREST
    );

    const getAlert = (alertId: number): Promise<Alert | undefined> => {
        return makeRequest(alertId);
    };

    return { alert: data, getAlert, status, errorMessage };
};
