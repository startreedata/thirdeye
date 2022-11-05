import { ActionHook } from "../actions.interfaces";
import { AppAnalytics } from "../dto/app-analytics.interfaces";

export interface GetAppAnalytics extends ActionHook {
    appAnalytics: AppAnalytics | null;
    getAppAnalytics: () => Promise<AppAnalytics | undefined>;
}
