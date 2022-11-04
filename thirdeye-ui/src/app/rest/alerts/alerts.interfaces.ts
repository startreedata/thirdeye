import { ActionHook } from "../actions.interfaces";
import {
    Alert,
    AlertEvaluation,
    AlertInsight,
    EditableAlert,
} from "../dto/alert.interfaces";
import { EnumerationItemParams } from "../dto/detection.interfaces";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";

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
        filters?: string[],
        enumerationItem?: { id: number } | EnumerationItem
    ) => Promise<AlertEvaluation | undefined>;
}

export interface GetAlert extends ActionHook {
    alert: Alert | null;
    getAlert: (id: number) => Promise<Alert | undefined>;
}

export interface GetAlerts extends ActionHook {
    alerts: Alert[] | null;
    getAlerts: () => Promise<Alert[] | undefined>;
}

export interface GetAlertInsight extends ActionHook {
    alertInsight: AlertInsight | null;
    getAlertInsight: ({
        alertId,
        alert,
    }: {
        alertId?: number;
        alert?: EditableAlert;
    }) => Promise<AlertInsight | undefined>;
}

export interface ResetAlert extends ActionHook {
    alert: Alert | null;
    resetAlert: (id: number) => Promise<Alert | undefined>;
}

export interface GetEvaluationRequestPayload extends UseGetEvaluationParams {
    evaluationContext?: {
        filters?: string[];
        enumerationItem?:
            | {
                  id: number;
              }
            | EnumerationItemParams;
    };
}
