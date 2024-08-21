/*
 * Copyright 2024 StarTree Inc
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
import { FeedbackClass } from "./investigations.interfaces";

export const getFeedbackText = (
    feedback: string | undefined
): string | undefined => {
    if (feedback === "ANOMALY") {
        return "Yes, this is a valid anomaly";
    } else if (feedback === "NOT_ANOMALY") {
        return "No, this is not an anomaly";
    } else if (!feedback) {
        return "No feedback present";
    } else {
        return feedback;
    }
};

export const getFeedbackClass = (
    feedback: string | undefined
): FeedbackClass => {
    if (feedback === "ANOMALY") {
        return "validAnomaly";
    } else if (feedback === "NOT_ANOMALY") {
        return "invalidAnomaly";
    } else {
        return "text";
    }
};
