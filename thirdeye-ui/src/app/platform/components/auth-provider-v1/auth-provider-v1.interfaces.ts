// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface AuthProviderV1Props {
    clientId?: string;
    redirectMethod: AuthRedirectMethodV1;
    oidcIssuerAudiencePath?: string;
    oidcIssuerLogoutPath?: string;
    redirectPathBlacklist?: string[];
    appInitFailure?: boolean;
    className?: string;
    children?: ReactNode;
}

export interface AuthProviderV1ContextProps {
    authenticated: boolean;
    authDisabled: boolean;
    accessToken: string;
    authDisabledNotification: boolean;
    authExceptionCode: string;
    login: () => void;
    logout: () => void;
    clearAuthDisabledNotification: () => void;
}

export enum AuthRedirectMethodV1 {
    Post = "POST",
    Get = "GET",
}

export enum AuthHashParamV1 {
    AccessToken = "access_token",
}

export enum AuthQueryParamV1 {
    AutomaticLogout = "automatic-logout",
    LoggedOut = "logged-out",
}

export enum AuthExceptionCodeV1 {
    AppInitFailure = "001",
    InitFailure = "002",
    InfoCallFailure = "003",
    OpenIDConfigurationCallFailure = "004",
    InfoMissing = "005",
    OpenIDConfigurationMissing = "006",
    UnauthorizedAccess = "007",
}

export enum AuthActionV1 {
    TriggerReload = "TRIGGER_RELOAD",
    TriggerLocationRedirect = "TRIGGER_LOCATION_REDIRECT",
    TriggerAuthLoginRedirect = "TRIGGER_AUTH_LOGIN_REDIRECT",
    TriggerAuthLogoutRedirect = "TRIGGER_AUTH_LOGOUT_REDIRECT",
}
