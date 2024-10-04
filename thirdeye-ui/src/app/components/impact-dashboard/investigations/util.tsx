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
import { useTranslation } from "react-i18next";
import { FeedbackClass } from "./investigations.interfaces";

export const useGetFeedbackText = (): ((
    feedback: string | undefined
) => string | undefined) => {
    const { t } = useTranslation();
    const getFeedback = (feedback: string | undefined): string | undefined => {
        switch (feedback) {
            case "ANOMALY":
                return t(
                    "pages.impact-dashboard.sections.investigations.labels.valid-anomaly-text"
                );
            case "NOT_ANOMALY":
                return t(
                    "pages.impact-dashboard.sections.investigations.labels.invalid-anomaly-text"
                );
            case undefined:
                return t(
                    "pages.impact-dashboard.sections.investigations.labels.no-feedback-text"
                );
            default:
                return feedback;
        }
    };

    return getFeedback;
};

export const getFeedbackClass = (
    feedback: string | undefined
): FeedbackClass => {
    switch (feedback) {
        case "ANOMALY":
            return "validAnomaly";
        case "NOT_ANOMALY":
            return "invalidAnomaly";
        default:
            return "text";
    }
};
