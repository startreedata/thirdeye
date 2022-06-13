import { ActionHook } from "../actions.interfaces";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";

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
        evaluationParams: UseGetEvaluationParams,
        filters?: string[]
    ) => Promise<AlertEvaluation | undefined>;
}

export interface GetAlert extends ActionHook {
    alert: Alert | null;
    getAlert: (id: number) => Promise<Alert | undefined>;
}

export interface GetEvaluationRequestPayload extends UseGetEvaluationParams {
    evaluationContext?: {
        filters: string[];
    };
}
