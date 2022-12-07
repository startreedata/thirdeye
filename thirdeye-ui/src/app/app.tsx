/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { AppBarConfigProvider } from "./components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import {
    AppContainerV1,
    NotificationScopeV1,
    NotificationTypeV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "./platform/components";
import { AppRouter } from "./routers/app/app.router";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const {
        authDisabled,
        authDisabledNotification,
        clearAuthDisabledNotification,
    } = useAuthProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        if (authDisabled && authDisabledNotification) {
            // Auth disabled and user not yet notified
            notify(
                NotificationTypeV1.Info,
                t("message.authentication-disabled"),
                false,
                NotificationScopeV1.Global,
                clearAuthDisabledNotification
            );
        }
    }, [authDisabled, authDisabledNotification]);

    return (
        <AppContainerV1 name={t("label.thirdeye")}>
            <AppBarConfigProvider>
                <AppRouter />
            </AppBarConfigProvider>
        </AppContainerV1>
    );
};
