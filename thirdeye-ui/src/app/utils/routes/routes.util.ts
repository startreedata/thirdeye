import { getRecognizedQueryString } from "../params/params.util";

const PLACEHOLDER_ROUTE_ID = ":id";

export const AppRoute = {
    BASE: "/",
    HOME: "/home",
    ALERTS: "/alerts",
    ALERTS_ALL: "/alerts/all",
    ALERTS_DETAIL: `/alerts/detail/id/${PLACEHOLDER_ROUTE_ID}`,
    ALERTS_CREATE: "/alerts/create",
    ALERTS_UPDATE: `/alerts/update/id/${PLACEHOLDER_ROUTE_ID}`,
    ANOMALIES: "/anomalies",
    ANOMALIES_ALL: "/anomalies/all",
    ANOMALIES_DETAIL: `/anomalies/detail/id/${PLACEHOLDER_ROUTE_ID}`,
    CONFIGURATION: "/configuration",
    SUBSCRIPTION_GROUPS: "/configuration/subscriptionGroups",
    SUBSCRIPTION_GROUPS_ALL: "/configuration/subscriptionGroups/all",
    SUBSCRIPTION_GROUPS_DETAIL: `/configuration/subscriptionGroups/detail/id/${PLACEHOLDER_ROUTE_ID}`,
    SUBSCRIPTION_GROUPS_CREATE: "/configuration/subscriptionGroups/create",
    SUBSCRIPTION_GROUPS_UPDATE: `/configuration/subscriptionGroups/update/id/${PLACEHOLDER_ROUTE_ID}`,
    METRICS: "/configuration/metrics",
    METRICS_ALL: "/configuration/metrics/all",
    METRICS_DETAIL: `/configuration/metrics/detail/id/${PLACEHOLDER_ROUTE_ID}`,
    SIGN_IN: "/signIn",
    SIGN_OUT: "/signOut",
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

export const getAlertsDetailPath = (id: number): string => {
    let path: string = AppRoute.ALERTS_DETAIL;
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

export const getAnomaliesDetailPath = (id: number): string => {
    let path: string = AppRoute.ANOMALIES_DETAIL;
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

export const getSubscriptionGroupsDetailPath = (id: number): string => {
    let path: string = AppRoute.SUBSCRIPTION_GROUPS_DETAIL;
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

export const getMetricsPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.METRICS);
};

export const getMetricsAllPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.METRICS_ALL);
};

export const getMetricsDetailPath = (id: number): string => {
    let path: string = AppRoute.METRICS_DETAIL;
    path = path.replace(PLACEHOLDER_ROUTE_ID, `${id}`);

    return createPathWithRecognizedQueryString(path);
};

export const getSignInPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.SIGN_IN);
};

export const getSignOutPath = (): string => {
    return createPathWithRecognizedQueryString(AppRoute.SIGN_OUT);
};

// Creates path with only the recognized app query string key-value pairs from URL that are allowed
// to be carried forward when navigating
export const createPathWithRecognizedQueryString = (path: string): string => {
    return `${path}?${getRecognizedQueryString()}`;
};
