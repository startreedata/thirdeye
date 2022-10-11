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
import { ActionHook } from "../actions.interfaces";
import {
    Alert,
    AlertEvaluation,
    AlertInsight,
    EditableAlert,
} from "../dto/alert.interfaces";

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
        filters: string[];
    };
}
