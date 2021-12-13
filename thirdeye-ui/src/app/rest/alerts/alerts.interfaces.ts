import { ActionHook } from "../actions.interfaces";
import { AlertEvaluation } from "../dto/alert.interfaces";
import { UseGetEvaluationParams } from "./alerts.actions";

export interface GetEvaluation extends ActionHook {
    evaluation?: AlertEvaluation;
    getEvaluation: (
        evaluationParams: UseGetEvaluationParams
    ) => Promise<AlertEvaluation | undefined>;
}
