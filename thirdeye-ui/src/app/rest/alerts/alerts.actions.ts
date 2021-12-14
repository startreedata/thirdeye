import { useHTTPAction } from "../create-rest-action";
import { AlertEvaluation } from "../dto/alert.interfaces";
import { GetEvaluation, UseGetEvaluationParams } from "./alerts.interfaces";
import { getAlertEvaluation } from "./alerts.rest";

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
