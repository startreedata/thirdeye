// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface AuthProviderV1Props {
    disableAuthOverride?: boolean;
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
