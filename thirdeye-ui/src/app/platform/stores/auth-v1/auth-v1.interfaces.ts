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
    enableAuthDisabledNotification: () => void;
}
