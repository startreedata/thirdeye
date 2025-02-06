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
import { AppBarConfigProvider } from "./components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { AnalyticsAndErrorReportingProviderV1 } from "./components/third-party-lib-setup-providers/analytics-and-error-reporting-provider-v1/analytics-and-error-reporting-provider-v1.component";
import { AppCrashPage } from "./pages/app-crash-page/app-crash-page.component";
import { AppContainerV1, useAuthProviderV1 } from "./platform/components";
import { AppRouter } from "./routers/app/app.router";
import ErrorBoundary from '../app/routers/app/error-boundary'
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 0,
            refetchOnWindowFocus: false,
        },
    },
});

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const { authDisabled, authDisabledNotification } = useAuthProviderV1();
    const { t } = useTranslation();

    useEffect(() => {
        if (authDisabled && authDisabledNotification) {
            // eslint-disable-next-line no-console
            console.log(t("message.authentication-disabled"));
        }
    }, [authDisabled, authDisabledNotification]);

    return (
        <QueryClientProvider client={queryClient}>
            <AnalyticsAndErrorReportingProviderV1>
                <AppContainerV1 name={t("label.thirdeye")}>
                    <Sentry.ErrorBoundary fallback={<AppCrashPage />}>
                    <ErrorBoundary>
                        <AppBarConfigProvider>
                            <AppRouter />
                        </AppBarConfigProvider>
                    </ErrorBoundary>
                    </Sentry.ErrorBoundary>
                </AppContainerV1>
            </AnalyticsAndErrorReportingProviderV1>
        </QueryClientProvider>
    );
};
