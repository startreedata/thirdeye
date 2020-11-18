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
    SIGN_IN: "/signIn",
    SIGN_OUT: "/signOut",
    PAGE_NOT_FOUND: "/pageNotFound",
} as const;

export const getBasePath = (): string => {
    return AppRoute.BASE;
};

export const getHomePath = (): string => {
    return AppRoute.HOME;
};

export const getAlertsPath = (): string => {
    return AppRoute.ALERTS;
};

export const getAlertsAllPath = (): string => {
    return AppRoute.ALERTS_ALL;
};

export const getAlertsDetailPath = (id: number): string => {
    let path: string = AppRoute.ALERTS_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, id.toString());

    return path;
};

export const getAlertsCreatePath = (): string => {
    return AppRoute.ALERTS_CREATE;
};

export const getAlertsUpdatePath = (id: number): string => {
    let path: string = AppRoute.ALERTS_UPDATE;
    path = path.replace(ROUTE_PLACEHOLDER_ID, id.toString());

    return path;
};

export const getAnomaliesPath = (): string => {
    return AppRoute.ANOMALIES;
};

export const getAnomaliesAllPath = (): string => {
    return AppRoute.ANOMALIES_ALL;
};

export const getAnomaliesDetailPath = (id: number): string => {
    let path: string = AppRoute.ANOMALIES_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, id.toString());

    return path;
};

export const getSignInPath = (): string => {
    return AppRoute.SIGN_IN;
};

export const getSignOutPath = (): string => {
    return AppRoute.SIGN_OUT;
};

export const getPageNotFoundPath = (): string => {
    return AppRoute.PAGE_NOT_FOUND;
};
