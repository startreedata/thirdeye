import { ActionStatus } from "../../../rest/actions.interfaces";
import { AppAnalytics } from "../../../rest/dto/app-analytics.interfaces";

export interface AnomaliesReportedCountProps {
    appAnalytics: AppAnalytics | null;
    getAppAnalyticsStatus: ActionStatus;
}
