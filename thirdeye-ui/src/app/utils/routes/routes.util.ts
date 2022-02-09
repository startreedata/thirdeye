import { getRecognizedQueryString } from "../params/params.util";

const PLACEHOLDER_ROUTE_ID = ":id";

export const AppRoute = {
    BASE: "/",
    HOME: "/home",
    ALERTS: "/alerts",
    ALERTS_ALL: "/alerts/all",
    ALERTS_VIEW: `/alerts/view/id/${PLACEHOLDER_ROUTE_ID}`,
    ALERTS_CREATE: "/alerts/create",
    ALERTS_UPDATE: `/alerts/update/id/${PLACEHOLDER_ROUTE_ID}`,
    ANOMALIES: "/anomalies",
    ANOMALIES_ALL: "/anomalies/all",
    ANOMALIES_VIEW: `/anomalies/view/id/${PLACEHOLDER_ROUTE_ID}`,
    ANOMALIES_VIEW_INDEX: `/anomalies/view/id/${PLACEHOLDER_ROUTE_ID}/index`,
    CONFIGURATION: "/configuration",
    SUBSCRIPTION_GROUPS: "/configuration/subscription-groups",
    SUBSCRIPTION_GROUPS_ALL: "/configuration/subscription-groups/all",
    SUBSCRIPTION_GROUPS_VIEW: `/configuration/subscription-groups/view/id/${PLACEHOLDER_ROUTE_ID}`,
    SUBSCRIPTION_GROUPS_CREATE: "/configuration/subscription-groups/create",
    SUBSCRIPTION_GROUPS_UPDATE: `/configuration/subscription-groups/update/id/${PLACEHOLDER_ROUTE_ID}`,
    DATASETS: "/configuration/datasets",
    DATASETS_ALL: "/configuration/datasets/all",
    DATASETS_VIEW: `/configuration/datasets/view/id/${PLACEHOLDER_ROUTE_ID}`,
    DATASETS_ONBOARD: "/configuration/datasets/onboard",
    DATASETS_UPDATE: `/configuration/datasets/update/id/${PLACEHOLDER_ROUTE_ID}`,
    DATASOURCES: "/configuration/datasources",
    DATASOURCES_ALL: "/configuration/datasources/all",
    DATASOURCES_VIEW: `/configuration/datasources/view/id/${PLACEHOLDER_ROUTE_ID}`,
    DATASOURCES_CREATE: "/configuration/datasources/create",
    DATASOURCES_UPDATE: `/configuration/datasources/update/id/${PLACEHOLDER_ROUTE_ID}`,
    METRICS: "/configuration/metrics",
    METRICS_ALL: "/configuration/metrics/all",
    METRICS_VIEW: `/configuration/metrics/view/id/${PLACEHOLDER_ROUTE_ID}`,
    METRICS_CREATE: `/configuration/metrics/create`,
    METRICS_UPDATE: `/configuration/metrics/update/id/${PLACEHOLDER_ROUTE_ID}`,
    LOGIN: "/login",
    LOGOUT: "/logout",
    ROOT_CAUSE_ANALYSIS: `/root-cause-analysis`,
    ROOT_CAUSE_ANALYSIS_FOR_ANOMALY: `/root-cause-analysis/anomaly/${PLACEHOLDER_ROUTE_ID}`,
    ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_INDEX: `/root-cause-analysis/anomaly/${PLACEHOLDER_ROUTE_ID}/index`,
} as const;

export const getBasePath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.BASE);
};

export const getHomePath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.HOME);
};

export const getAlertsPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.ALERTS);
};

export const getAlertsAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.ALERTS_ALL);
};

export const getAlertsViewPath = (id: number): string => {
    let path: string = AppRoute.ALERTS_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getAlertsCreatePath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.ALERTS_CREATE);
};

export const getAlertsUpdatePath = (id: number): string => {
    let path: string = AppRoute.ALERTS_UPDATE;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getAnomaliesPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.ANOMALIES);
};

export const getAnomaliesAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.ANOMALIES_ALL);
};

export const getAnomaliesViewPath = (id: number): string => {
    let path: string = AppRoute.ANOMALIES_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getAnomaliesViewIndexPath = (id: number): string => {
    let path: string = AppRoute.ANOMALIES_VIEW_INDEX;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getConfigurationPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.CONFIGURATION);
};

export const getSubscriptionGroupsPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.SUBSCRIPTION_GROUPS);
};

export const getSubscriptionGroupsAllPath = (): string => {
    return createPathWithRecognizedQueryString(
        AppRoute.SUBSCRIPTION_GROUPS_ALL
    );
};

export const getSubscriptionGroupsViewPath = (id: number): string => {
    let path: string = AppRoute.SUBSCRIPTION_GROUPS_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getSubscriptionGroupsCreatePath = (): string => {
    return createPathWithRecognizedQueryString(
        AppRoute.SUBSCRIPTION_GROUPS_CREATE
    );
};

export const getSubscriptionGroupsUpdatePath = (id: number): string => {
    let path: string = AppRoute.SUBSCRIPTION_GROUPS_UPDATE;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getDatasetsPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASETS_ALL);
};

export const getDatasetsAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASETS_ALL);
};

export const getDatasetsViewPath = (id: number): string => {
    let path: string = AppRoute.DATASETS_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getDatasetsOnboardPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASETS_ONBOARD);
};

export const getDatasetsUpdatePath = (id: number): string => {
    let path: string = AppRoute.DATASETS_UPDATE;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getDatasourcesPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASOURCES);
};

export const getDatasourcesAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASOURCES_ALL);
};

export const getDatasourcesViewPath = (id: number): string => {
    let path: string = AppRoute.DATASOURCES_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getDatasourcesCreatePath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.DATASOURCES_CREATE);
};

export const getDatasourcesUpdatePath = (id: number): string => {
    let path: string = AppRoute.DATASOURCES_UPDATE;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getMetricsPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.METRICS);
};

export const getMetricsAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.METRICS_ALL);
};

export const getMetricsViewPath = (id: number): string => {
    let path: string = AppRoute.METRICS_VIEW;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getMetricsCreatePath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.METRICS_CREATE);
};

export const getMetricsUpdatePath = (id: number): string => {
    let path: string = AppRoute.METRICS_UPDATE;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getRootCauseAnalysisForAnomalyPath = (id: number): string => {
    let path: string = AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getLoginPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.LOGIN);
};

export const getLogoutPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.LOGOUT);
};

// Creates path with only the recognized app query string key-value pairs from URL that are allowed
// to be carried forward when navigating
export const createPathWithRecognizedQueryString = (path: string): string => {
    return `${path}?${getRecognizedQueryString()}`;
};
