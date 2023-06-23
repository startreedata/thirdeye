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
import { ReactNode } from "react";
import { AnomalyFeedback } from "../../../rest/dto/anomaly.interfaces";

export interface AnomalyFeedbackModalProps {
    anomalyId: number;
    anomalyFeedback?: AnomalyFeedback;
    trigger: (callback: () => void) => ReactNode;
    showNo?: boolean;
    onFeedbackUpdate: (feedback: AnomalyFeedback) => void;
}

export const ANOMALY_FEEDBACK_TEST_IDS = {
    REASON_SELECTION: "reason-selection",
    SUBMIT_BTN: "submit-btn",
    COMMENT_INPUT: "comment-input",
};
