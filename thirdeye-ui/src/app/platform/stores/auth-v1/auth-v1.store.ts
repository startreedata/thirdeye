///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import create from "zustand";
import { persist } from "zustand/middleware";
import { AuthV1 } from "./auth-v1.interfaces";

const KEY_AUTH = "auth-v1";

// App store for auth, persisted in browser local storage
export const useAuthV1 = create<AuthV1>(
    persist(
        (set) => ({
            authenticated: false,
            authDisabled: false,
            authDisabledNotification: true,
            accessToken: "",
            authExceptionCode: "",
            redirectHref: "",
            authAction: "",
            authActionData: "",

            enableAuth: () => {
                set({
                    authDisabled: false,
                });
            },

            disableAuth: () => {
                set({
                    authenticated: false,
                    authDisabled: true,
                    accessToken: "",
                    authExceptionCode: "",
                });
            },

            enableAuthDisabledNotification: () => {
                set({
                    authDisabledNotification: true,
                });
            },

            clearAuthDisabledNotification: () => {
                set({
                    authDisabledNotification: false,
                });
            },

            setAccessToken: (token) => {
                set({
                    authenticated: Boolean(token),
                    authDisabled: false,
                    authDisabledNotification: true, // Auth disabled notification to be displayed next time auth is disabled
                    accessToken: token || "",
                    authExceptionCode: "",
                });
            },

            clearAuth: (exceptionCode) => {
                set({
                    authenticated: false,
                    authDisabled: false,
                    accessToken: "",
                    authExceptionCode: exceptionCode || "",
                });
            },

            clearAuthException: () => {
                set({
                    authExceptionCode: "",
                });
            },

            setRedirectHref: (href) => {
                set({
                    redirectHref: href,
                });
            },

            clearRedirectHref: () => {
                set({
                    redirectHref: "",
                });
            },

            setAuthAction: (action, actionData) => {
                set({
                    authAction: action || "",
                    authActionData: actionData || "",
                });
            },

            clearAuthAction: () => {
                set({
                    authAction: "",
                    authActionData: "",
                });
            },
        }),
        {
            name: KEY_AUTH, // Persist in browser local storage
            blacklist: ["authAction", "authActionData"], // Prevent persisting in state
        }
    )
);
