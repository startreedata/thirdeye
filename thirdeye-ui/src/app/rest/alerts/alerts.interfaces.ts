import { ActionHook } from "../actions.interfaces";
import { AlertEvaluation } from "../dto/alert.interfaces";

export interface UseGetEvaluationParams {
    alert: {
        id: number;
    };
    start: number;
    end: number;
}

export interface GetEvaluation extends ActionHook {
    evaluation: AlertEvaluation | null;
    getEvaluation: (
        evaluationParams: UseGetEvaluationParams
    ) => Promise<AlertEvaluation | undefined>;
}
