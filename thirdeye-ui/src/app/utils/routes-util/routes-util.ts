import { getTimeRangeQueryString } from "../params-util/params-util";

const ROUTE_PLACEHOLDER_ID = ":id";

export const AppRoute = {
    BASE: "/",
    HOME: "/home",
    ALERTS: "/alerts",
    ALERTS_ALL: "/alerts/all",
    ALERTS_DETAIL: `/alerts/id/${ROUTE_PLACEHOLDER_ID}`,
    ALERTS_CREATE: "/alerts/create",
    ALERTS_UPDATE: `/alerts/update/id/${ROUTE_PLACEHOLDER_ID}`,
    ANOMALIES: "/anomalies",
    ANOMALIES_ALL: "/anomalies/all",
    ANOMALIES_DETAIL: `/anomalies/id/${ROUTE_PLACEHOLDER_ID}`,
    CONFIGURATION: "/configuration",
    SIGN_IN: "/signIn",
    SIGN_OUT: "/signOut",
} as const;

export const getBasePath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.BASE);
};

export const getHomePath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.HOME);
};

export const getAlertsPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.ALERTS);
};

export const getAlertsAllPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.ALERTS_ALL);
};

export const getAlertsDetailPath = (id: number): string => {
    let path: string = AppRoute.ALERTS_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return createPathWithTimeRangeQueryString(path);
};

export const getAlertsCreatePath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.ALERTS_CREATE);
};

export const getAlertsUpdatePath = (id: number): string => {
    let path: string = AppRoute.ALERTS_UPDATE;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return createPathWithTimeRangeQueryString(path);
};

export const getAnomaliesPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.ANOMALIES);
};

export const getAnomaliesAllPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.ANOMALIES_ALL);
};

export const getAnomaliesDetailPath = (id: number): string => {
    let path: string = AppRoute.ANOMALIES_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return createPathWithTimeRangeQueryString(path);
};

export const getConfigurationPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.CONFIGURATION);
};

export const getSignInPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.SIGN_IN);
};

export const getSignOutPath = (): string => {
    return createPathWithTimeRangeQueryString(AppRoute.SIGN_OUT);
};

// Picks up time range query string from URL and ataches it to new path
export const createPathWithTimeRangeQueryString = (path: string): string => {
    return `${path}?${getTimeRangeQueryString()}`;
};
