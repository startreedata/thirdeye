import { useHTTPAction } from "../create-rest-action";
import { AppAnalytics } from "../dto/app-analytics.interfaces";
import { GetAppAnalytics } from "./app-analytics.interfaces";
import { getAppAnalytics as getAppAnalyticsREST } from "./app-analytics.rest";

export const useGetAppAnalytics = (): GetAppAnalytics => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AppAnalytics>(getAppAnalyticsREST);

    const getAppAnalytics = (): Promise<AppAnalytics | undefined> => {
        return makeRequest();
    };

    return { appAnalytics: data, getAppAnalytics, status, errorMessages };
};
