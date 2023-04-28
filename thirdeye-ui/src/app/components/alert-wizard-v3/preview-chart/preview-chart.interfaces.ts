/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React from "react";
import { ActionStatus } from "../../../rest/actions.interfaces";
import {
    AlertEvaluation,
    EditableAlert,
} from "../../../rest/dto/alert.interfaces";

export interface PreviewChartProps {
    alert: EditableAlert;
    showLoadButton: boolean;
    onAlertPropertyChange: (
        contents: Partial<EditableAlert>,
        isTotalReplace?: boolean
    ) => void;
    fetchOnInitialRender?: boolean;
    headerComponent?: (
        evaluation: AlertEvaluation | null,
        evaluationRequestStatus: ActionStatus
    ) => React.ReactNode;
    onEvaluationFetchStart?: (startTime: number, endTime: number) => void;
}
