export const AppRoute = {
    BASE: "/",
    ALERTS: "/alerts",
    ALERTS_DETAIL: "/alerts/:id",
    ALERTS_EDIT: "/alerts/edit/:id",
    ALERTS_ALL: "/alerts/all",
    SIGN_IN: "/signIn",
} as const;

export const getBasePath = (): string => {
    return AppRoute.BASE;
};

export const getAlertsPath = (): string => {
    return AppRoute.ALERTS;
};

export const getAlertsAllPath = (): string => {
    return AppRoute.ALERTS_ALL;
};

export const getAlertsDetailsPath = (): string => {
    return AppRoute.ALERTS_DETAIL;
};

export const getAlertsEditPath = (): string => {
    return AppRoute.ALERTS_EDIT;
};

export const getSignInPath = (): string => {
    return AppRoute.SIGN_IN;
};
