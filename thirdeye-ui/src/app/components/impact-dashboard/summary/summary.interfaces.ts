import { VerboseSummary } from "../../../pages/impact-dashboard-page/impact-dashboard-page.interfaces";

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
export interface SummaryProps {
    summaryData: {
        alerts: {
            activeAlerts: number;
            activeDimensions: number;
        };
        anomalies: {
            detected: number;
            investigations: number;
        };
        notifications: {
            notificationsSent: number;
            groups: number;
        };
    };
    verboseSummaryItems: VerboseSummary;
    selectedAnalysisPeriod: string;
    onAnalysisPeriodChange: (period: string) => void;
    analysisPeriods: string[];
}
