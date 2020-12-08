const ROUTE_PLACEHOLDER_ID = ":id";

export const ApplicationRoute = {
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
    return ApplicationRoute.BASE;
};

export const getHomePath = (): string => {
    return ApplicationRoute.HOME;
};

export const getAlertsPath = (): string => {
    return ApplicationRoute.ALERTS;
};

export const getAlertsAllPath = (): string => {
    return ApplicationRoute.ALERTS_ALL;
};

export const getAlertsDetailPath = (id: number): string => {
    let path: string = ApplicationRoute.ALERTS_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return path;
};

export const getAlertsCreatePath = (): string => {
    return ApplicationRoute.ALERTS_CREATE;
};

export const getAlertsUpdatePath = (id: number): string => {
    let path: string = ApplicationRoute.ALERTS_UPDATE;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return path;
};

export const getAnomaliesPath = (): string => {
    return ApplicationRoute.ANOMALIES;
};

export const getAnomaliesAllPath = (): string => {
    return ApplicationRoute.ANOMALIES_ALL;
};

export const getAnomaliesDetailPath = (id: number): string => {
    let path: string = ApplicationRoute.ANOMALIES_DETAIL;
    path = path.replace(ROUTE_PLACEHOLDER_ID, `${id}`);

    return path;
};

export const getConfigurationPath = (): string => {
    return ApplicationRoute.CONFIGURATION;
};

export const getSignInPath = (): string => {
    return ApplicationRoute.SIGN_IN;
};

export const getSignOutPath = (): string => {
    return ApplicationRoute.SIGN_OUT;
};
