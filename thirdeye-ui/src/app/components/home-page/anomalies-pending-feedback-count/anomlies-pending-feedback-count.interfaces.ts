import { ActionStatus } from "../../../rest/actions.interfaces";
import { AppAnalytics } from "../../../rest/dto/app-analytics.interfaces";

export interface AnomaliesPendingFeedbackCountProps {
    appAnalytics: AppAnalytics | null;
    getAppAnalyticsStatus: ActionStatus;
}
