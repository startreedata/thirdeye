// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface AuthV1 {
    authenticated: boolean;
    authDisabled: boolean;
    authDisabledNotification: boolean;
    accessToken: string;
    authExceptionCode: string;
    redirectHref: string;
    authAction: string;
    authActionData: string;
    enableAuth: () => void;
    disableAuth: () => void;
    clearAuthDisabledNotification: () => void;
    setAccessToken: (token: string) => void;
    clearAuth: (exceptionCode?: string) => void;
    clearAuthException: () => void;
    setRedirectHref: (href: string) => void;
    clearRedirectHref: () => void;
    setAuthAction: (action: string, actionData?: string) => void;
    clearAuthAction: () => void;
}
