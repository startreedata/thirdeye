import axios from "axios";
import { useHTTPAction } from "../create-rest-action";
import { AlertEvaluation } from "../dto/alert.interfaces";
import { GetEvaluation } from "./alerts.interfaces";
import { BASE_URL_ALERTS } from "./alerts.rest";

export interface UseGetEvaluationParams {
    alert: {
        id: number;
    };
    start: number;
    end: number;
}

export const useGetEvaluation = (): GetEvaluation => {
    const {
        data,
        makeRequest,
        status,
        errorMessage,
    } = useHTTPAction<AlertEvaluation>(axios.post);

    const getEvaluation = (
        evaluationParams: UseGetEvaluationParams
    ): Promise<AlertEvaluation | undefined> => {
        return makeRequest(`${BASE_URL_ALERTS}/evaluate`, evaluationParams);
    };

    return { evaluation: data, getEvaluation, status, errorMessage };
};
