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

import { AlertEffectivnessData } from "../../../pages/impact-dashboard-page/impact-dashboard-page.interfaces";
import { Alert } from "../../../rest/dto/alert.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { Investigation } from "../../../rest/dto/rca.interfaces";
import { SubscriptionGroup } from "../../../rest/dto/subscription-group.interfaces";

export interface SummaryDataProps {
    mostRecentlyInvestigatedAnomalyAlert?: Pick<Alert, "id" | "name">;
    anomalies: Anomaly[] | null;
    previousPeriodAnomalies: Anomaly[] | null;
    topAlert: AlertEffectivnessData;
    investigations: Investigation[] | null;
    alertsCount: { count: number } | null;
    subscriptionGroups: SubscriptionGroup[] | null;
    selectedAnalysisPeriod: string;
}

export interface SummaryProps extends SummaryDataProps {
    onAnalysisPeriodChange: (period: string) => void;
    analysisPeriods: string[];
}

export type SummaryData = {
    alerts: {
        activeAlerts: { count: number; href: string };
        activeDimensions: { count: number; href: string };
    };
    anomalies: {
        detected: { count: number; href: string };
        investigations: { count: number; href: string };
    };
    notifications: {
        notificationsSent: { count: number; href: string };
        groups: { count: number; href: string };
    };
};

export type VerboseSummary = {
    weeks: string;
    percentageChange: string;
    topAlert: {
        id: number | null;
        name: string;
        anomaliesCount: number;
    };
    investigation: {
        count: number;
        date: string;
        alert: {
            id: number | null | undefined;
            name: string | undefined;
        };
    };
};

export type Summary = {
    summaryData: SummaryData;
    verboseSummaryItems: VerboseSummary;
};
