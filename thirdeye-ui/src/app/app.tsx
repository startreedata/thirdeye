/*
 * Copyright 2023 StarTree Inc
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

import * as Sentry from "@sentry/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { AnalyticsAndErrorReportingProviderV1 } from "./components/analytics-and-error-reporting-provider-v1/analytics-and-error-reporting-provider-v1.component";
import { AppBarConfigProvider } from "./components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { AppCrashPage } from "./pages/app-crash-page/app-crash-page.component";
import {
    AppContainerV1,
    NotificationScopeV1,
    NotificationTypeV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "./platform/components";
import { AppRouter } from "./routers/app/app.router";

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 0,
        },
    },
});

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
        <QueryClientProvider client={queryClient}>
            <AnalyticsAndErrorReportingProviderV1>
                <AppContainerV1 name={t("label.thirdeye")}>
                    <Sentry.ErrorBoundary fallback={<AppCrashPage />}>
                        <AppBarConfigProvider>
                            <AppRouter />
                        </AppBarConfigProvider>
                    </Sentry.ErrorBoundary>
                </AppContainerV1>
            </AnalyticsAndErrorReportingProviderV1>
        </QueryClientProvider>
    );
};
